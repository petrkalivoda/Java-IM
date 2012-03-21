/**
 * node klienta, umoznuje praci s nim
 */

package JIM.server;
import java.util.ArrayList;
/**
 *
 * @author cypher
 */

public class ClientNode {
    private String username = null; //uzivatelske jmeno
    private String password = null; //heslo
    private String name = null; //jmeno
    private String surname = null; //prijmeni
    private ArrayList<String[]> contacts = new ArrayList<String[]>(); //kontakt list


    /**
     * vytvari node z udaju z databaze
     * @param un
     * @param pw
     * @param n
     * @param sn
     */
    public ClientNode(String un, String pw, String n, String sn)
    {
        username = un;
        password = pw;
        name = n;
        surname = sn;
        
    }

    /**
     * getter na uzivatelske jmeno
     * @return
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * odstrani kontakt ze seznamu
     * @param username
     */
    public void removeContact(String username)
    {
        int i = 0;
        while(i < contacts.size())
        {
            if(contacts.get(i)[0].equals(username))
            {
                contacts.remove(i);
                break;
            }
            i++;
        }
    }

    /**
     * prida kontakt do seznamu kontaktu
     * @param username
     * @param name
     * @param surname
     * @throws Exception pokud tam kontakt uz je
     */
    public void addContact(String username, String name, String surname) throws Exception
    {
        int i = 0;
        String[] contact = new String[3];
        while(i < contacts.size())
        {
            if(contacts.get(i)[0].equals(username)) throw new Exception("Kontakt "+username+" už v seznamu kontaktů je.");
            i++;
        }

       contact[0] = username;
       contact[1] = name;
       contact[2] = surname;
        contacts.add(contact);
    }

    /**
     * gettery na udaje klienta
     * @return
     */
    public String getPassword()
    {
        return password;
    }

    public String getName()
    {
        return name;
    }

    public String getSurname()
    {
        return surname;
    }

    @Override
    /**
     * vytvori xml string klienta, ktery se odesila po jeho prihlaseni
     */
    public String toString()
    {
        String xmlString = new String();
        //xmlString += "<?xml version='1.0'?>";
        xmlString += "<client>";
        xmlString += "<username>"+username+"</username>";
        xmlString += "<password>"+password+"</password>";
        xmlString += "<name>"+name+"</name>";
        xmlString += "<surname>"+surname+"</surname>";
        xmlString += "<contacts>";
        for(int i = 0; i < contacts.size(); i++)
        {
            String[] contact = contacts.get(i);
            xmlString += "<contact username='"+contact[0]+"' name='"+contact[1]+"' surname='"+contact[2]+"' state='";
            if(TCPConnection.isOnline(contact[0])) xmlString += "online";
            else xmlString += "offline";
            xmlString += "' />";
        }
        xmlString += "</contacts>";
        xmlString += "</client>";

        return xmlString;
    }

    /**
     * vrati klienta jako kontakt
     * @return
     */
    public String getAsContact()
    {
        String state = TCPConnection.isOnline(username) ? "online" : "offline";
        return "<contact username='"+username+"' name='"+name+"' surname='"+surname+"' state='"+state+"' />";
    }
}
