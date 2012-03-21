/**
 * thread TCP komunikace s klientem
 */
package JIM.server;

import java.io.*;
import java.net.*;

/**
 *
 * @author cypher
 */
public class TCPCommunication extends Thread {

    private Socket socket = null;
    private static int connections_made = 0; //celkovy pocet navazanych komunikaci za beh serveru
    private int connection_id = 0; //id spojeni
    private PrintWriter p;
    private String logged_as = new String(); //uzivatelske jmeno po prihlaseni

    public TCPCommunication(Socket s) {
        super("ServerSocket #" + ++connections_made);
        connection_id = connections_made;
        socket = s;
        TCPConnection.threads.add(this);
        System.out.println("Connected client #" + connection_id);
        System.out.println("Alive connections: " + ++TCPConnection.number_of_connections);
    }

    @Override

    /**
     * zpracovava requesty, neceka na zadnou odpoved, odpovida na vsechny zname requesty
     * aspon prazdnou zpravou. Nektere pozadavky ktere potrebuji zvlastni zachazeni
     * jsou zde jeste upravovany pred/po Protocolu.
     */
    public void run() {
        try {
            p = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String in;
            TCPResponse r;

            //p.println("Ahoj, tvoje id je #"+connection_id);
            while ((in = br.readLine()) != null) {
                r = Protocol.handle(in);
                boolean standardResponse = true; //jestli se ma odpovidat standardne podle protokolu


                //prihlasovani
                if (r.getType().equals("login")) {
                    if (r.toString().indexOf("state='ok'") >= 0) {
                        logged_as = r.getRecipient().toLowerCase();
                    }

                }

                //zprava
                if (r.getType().equals("message")) {
                    r.addAttribute("from", logged_as);

                    java.util.Calendar cal = java.util.Calendar.getInstance();
                    Long time = cal.getTimeInMillis();
                    standardResponse = false;
                    try {
                        TCPConnection.send(r.getRecipient(), r.toString());
                        p.println("<msg type='delivery-state' timestamp='" + time + "' state='ok' />");
                    } catch (Exception e) {
                        p.println("<msg type='delivery-state' timestamp='" + time + "' state='failed' error='Příjemce zprávy je buď offline, nebo neexistuje.' />");
                    }
                }

                //odhlaseni
                if (r.getType().equals("signoff")) {
                    p.println("<msg type='bye' />");
                    break;
                }


                //pridani kontaktu
                if(r.getType().equals("contact-added"))
                {
                    standardResponse = false;
                    try
                    {
                        TCPConnection.send(r.getRecipient(), r.toString());
                        
                    }catch(Exception e){}
                    finally
                    {
                        p.println("<msg type='contact-add-response' />");
                    }
              
                }

                //odstraneni kontaktu
                if(r.getType().equals("contact-removed"))
                {
                    standardResponse = false;
                    try
                    {
                        TCPConnection.send(r.getRecipient(), r.toString());

                    }catch(Exception e){}
                    finally
                    {
                        p.println("<msg type='contact-remove-response' />");
                    }

                }


                if(standardResponse == true)p.println(r.toString());

            }

            p.close();
            br.close();
            socket.close();
            System.out.println("Client #" + connection_id + " has left.");

        } catch (IOException e) {
            System.out.println("Client #" + connection_id + " has timed out.");
        } finally {
            TCPConnection.number_of_connections--;
            TCPConnection.threads.remove(this);
            if (logged_as.length() > 0) {
                TCPConnection.stateChangeNotify(logged_as, "offline");
            }
        }
    }

    /**
     * posila zpravu klientovi, neceka na zadnou odpoved
     * @param msg
     */
    public void send(String msg) {
        p.println(msg);
    }


    /**
     * getter na uzivatelske jmeno prihlaseneho
     * @return
     */
    public String getLoggedAs() {
        return logged_as;
    }
}
