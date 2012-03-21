/**
 * zabyva se praci s thready komunikace
 */

package JIM.server;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
/**
 *
 * @author cypher
 */
public class TCPConnection {
    private static ServerSocket ss = null;
    private static boolean iListen = false;
    public static final int PORT = 2188;
    public static int number_of_connections; //pocet spojeni
    public static ArrayList<TCPCommunication> threads = new ArrayList<TCPCommunication>(); //array list threadu komunikace
    
    public static void main(String[] args) throws IOException {
        try
        {
            ss = new ServerSocket(PORT);
            iListen = true;
        }catch(IOException e){
            System.err.println("Nemůžu naslouchat na portu "+PORT+".");
            System.exit(-1);
        }

        while(iListen) new TCPCommunication(ss.accept()).start();

    }

    /**
     * posila zpravu klientovi podle jeho uzivatelskeho jmena
     * @param recipient
     * @param msg
     * @throws Exception pokud recipient neexistuje (=je offline)
     */
    public static void send(String recipient, String msg) throws Exception
    {
        int i = 0;
        boolean sent = false;
        TCPCommunication c = null;
        while(i < threads.size())
        {
            c = threads.get(i);
            if(c.getLoggedAs().equals(recipient.toLowerCase()))
            {
                c.send(msg);
                sent = true;
                break;
            }
            i++;
        }

        if(sent == false) throw new Exception("Recipient not found");
    }

    /**
     * zjistuje jestli je dane uzivatelske jmeno online
     * @param name
     * @return
     */
    public static boolean isOnline(String name)
    {
        TCPCommunication c = null;
        boolean isOnline = false;
        int i = 0;
        while(i < threads.size())
        {
            c = threads.get(i);
            if(c.getLoggedAs().equals(name.toLowerCase()))
            {
                isOnline = true;
                break;
            }
            i++;
        }

        return isOnline;
    }

    /**
     * notifikuje vsechny o zmene stavu uzivatele (krome jeho samotneho)
     * @param user
     * @param state
     */
    public static void stateChangeNotify(String user, String state)
    {
        for(int i = 0; i < threads.size(); i++)
        {
            TCPCommunication c = threads.get(i);
            if(!c.getLoggedAs().toLowerCase().equals(user.toLowerCase())) c.send("<msg type='state-change' from='"+user+"' state='"+state+"' />");
        }
    }

}
