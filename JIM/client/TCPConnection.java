/**
 * trida umoznujici TCP komunikaci se serverem
 */
package JIM.client;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 *
 * @author cypher
 */
public class TCPConnection extends Thread {

    private static TCPConnection instance = null; //singleton
    public static String serverIP = null; //ip serveru
    public static final int PORT = 2188; //port (nemenny, vybral jsem ho z http://www.iana.org/assignments/port-numbers,
                                         //kde je uvedeny jako unassigned
    private Socket socket = null; 
    private PrintWriter p = null;
    private BufferedReader br = null;
    private String response = "NIC"; //prvotni odpoved serveru (kvuli prohledavani zmen)
    private ArrayList<Contact> contacts = new ArrayList<Contact>(); //kontakt list


    /**
     * konstruktor vytvari spojeni
     */
    private TCPConnection() {
        super();
        try {
            socket = new Socket(serverIP, PORT);
            p = new PrintWriter(socket.getOutputStream(), true);
            br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.err.println("Hostitel " + serverIP + " nebyl nalezen.");
            System.exit(-1);
        } catch (IOException e) {
            System.err.println("Komunikaci s hostitelem " + serverIP + " se nepodařilo navázat.");
            System.exit(-1);
        }

    }

    /**
     * singleton
     * @return
     */
    public static TCPConnection getInstance() {
        if (!(TCPConnection.instance instanceof TCPConnection)) {
            TCPConnection.instance = new TCPConnection();
            TCPConnection.instance.start();
        }

        return TCPConnection.instance;
    }

    @Override
    @SuppressWarnings("empty-statement")
    /**
     * thread komunikace, ceka na cokoliv ze serveru a pokud umi, zpracuje to
     * pokud ne, aspon to ulozi do this.response (metoda send totiz vyhledava
     * zmeny prave teto promenne kdyz ceka na odpoved serveru)
     */
    public void run() {

        String in;

        try {
            
            while ((in = br.readLine()) != null) {
                if (in.indexOf(" type='state-change'") > 0) { //zmena stavu kontaktu
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(in));
                    Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
                    String name = node.getAttribute("from");
                    String state = node.getAttribute("state");
                    changeContactState(name, state);
                }

                if (in.indexOf(" type='contact-added'") > 0) { //pridani kontaktu
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(in));
                    Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
                    Element contact = (Element) node.getElementsByTagName("contact").item(0);
                    Contact c = new Contact(contact.getAttribute("username"), contact.getAttribute("name"), contact.getAttribute("surname"));
                    c.setState(contact.getAttribute("state"));
                    addContact(c);
                    Main.refreshContacts();
                }

                if (in.indexOf(" type='contact-removed'") > 0) { //smazani kontaktu
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(in));
                    Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
                    String username = node.getAttribute("username");
                    removeContact(username);
                    Main.refreshContacts();
                }

                if (in.indexOf(" type='message'") > 0) { //zprava
                    InputSource is = new InputSource();
                    is.setCharacterStream(new StringReader(in));
                    Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
                    String from = node.getAttribute("from");
                    String content = node.getAttribute("content");
                    long time = Long.parseLong(node.getAttribute("timestamp"));
                    Contact c = getContactByUsername(from);
                    Message m = new Message(content, time);
                    m.setRecipient(c);
                    m.setType("in");
                    Main.incomingMessage(m);
                }

                response = new String(in);
            }

        } catch (Exception e) {
            System.err.println("Něco strašně IOumřelo.");
            System.exit(-1);
        }
    }

    @SuppressWarnings("empty-statement")
    /**
     * odesila zpravy serveru a ceka na odpoved.
     * server odpovida vzdy, takze v praxi cekame na zmenu
     * v promenne this.response (ktera diky timestampu zprav)
     * bude jina i kdyby byl obsah odpovedi stejny
     */
    public String send(String msg) {
        String old_response = new String(response);

        try {
            p.println(msg);
            while (old_response.equals(response)) {
                wait(100);
            }

        } catch (Exception e) {
            System.out.println("IO error.");
        }

        return response;
    }

    /**
     * odhlaseni
     */
    public void signoff() {
        Auth a = Auth.getInstance();
        String recipient = a.getUsername();
        java.util.Calendar cal = java.util.Calendar.getInstance();
        Long time = cal.getTimeInMillis();
        String re = send("<msg type='signoff' recipient='" + recipient + "' timestamp= '" + time + "' />");
        System.out.println(re);
        try {
            br.close();
            p.close();
            socket.close();
            instance = null;
            Auth.getInstance().logout();
        } catch (Exception e) {
        }
        stop();
    }

    /**
     * čeká n milisekund
     * @param n
     */
    private void wait(int n) {
        long t0, t1;
        t0 = System.currentTimeMillis();
        do {
            t1 = System.currentTimeMillis();
        } while ((t1 - t0) < (n));
    }

    public void addContact(Contact c) {
        int i = 0;
        boolean exists = false;
        while (i < contacts.size()) {
            if (c.getUsername().toLowerCase().equals(contacts.get(i).getUsername().toLowerCase())) {
                exists = true;
                break;
            }
            i++;
        }

        if (exists == false) {
            contacts.add(c);
        }
    }

    /**
     * odstraneni kontaktu
     * @param username
     */
    public void removeContact(String username) {
        int i = 0;
        while (i < contacts.size()) {
            if (contacts.get(i).getUsername().toLowerCase().equals(username)) {
                MessageWindow mw = Main.instance.getMessageWindow(contacts.get(i));
                if(mw instanceof MessageWindow)
                {
                    Main.instance.removeFromWinList(mw);
                    mw.dispose();
                }
                
                contacts.remove(i);
                break;
            }
            i++;
        }

    }

    /**
     * getter na contact list
     * @return
     */
    public ArrayList<Contact> getContacts() {
        return contacts;
    }


    /**
     * zpracuje zmenu stavu kontaktu
     * @param name
     * @param state
     */
    public void changeContactState(String name, String state) {
        int i = 0;
        boolean changed = false;
        Contact c;
        while (i < contacts.size()) {
            c = contacts.get(i);
            if (c.getUsername().equals(name.toLowerCase())) {
                changed = true;
                c.setState(state);
            }
            i++;
        }

        if (changed == true) {
            Main.refreshContacts();
        }
    }

    /**
     * getter na kontakt podle uzivatelskeho jmena
     * @param username
     * @return
     */
    public Contact getContactByUsername(String username) {
        int i = 0;
        Contact c = null;

        while (i < contacts.size()) {
            c = contacts.get(i);
            if (c.getUsername().equals(username)) {
                c = contacts.get(i);
                break;
            }
            i++;
        }

        return c;
    }
}
