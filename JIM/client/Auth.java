/**
 * Singleton třída umožňující autentifikaci uživatele
 * vůči serveru.
 */
package JIM.client;


import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author cypher
 */
public class Auth {

    private static Auth instance = null; //singleton
    private boolean logged = false; //jestli je uzivatel prihlasen
    private String username = new String(); //uzivatelske jmeno
    private String name = new String(); //jmeno
    private String surname = new String(); //prijmeni

    /**
     * odhlaseni
     */
    public void logout()
    {
        username = null;
        name = null;
        surname = null;
        instance = null;
    }

    //singleton
    private Auth() {
    }

    //ziska instanci tridy
    public static Auth getInstance() {
        if (!(Auth.instance instanceof Auth)) {
            Auth.instance = new Auth();
        }
        return Auth.instance;
    }

    /**
     * pokusi se prihlasit uzivatele podle vstupnich parametru
     *
     * @param username uzivatelske jmeno
     * @param password heslo
     * @throws Exception
     */
    public void login(String username, String password) throws Exception {
        
        TCPConnection c = TCPConnection.getInstance();
        
        String response = c.send("<msg type='login' recipient='" + username + "' username='" + username + "' password='" + password + "' />");
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(response));
        DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = df.newDocumentBuilder();
        Document d;
        Node root;

        d = db.parse(is);
        root = d.getDocumentElement();
        root.normalize();
        Element r = (Element) root;
        String state = r.getAttribute("state");
        if (!state.equals("ok")) {
            throw new Exception("Login failed.");
        } else {

            NodeList lst = d.getElementsByTagName("client");
            Node node = lst.item(0);

            //Element el = (Element) node;
            Element el = (Element) node;
            Element tmp_el;
            NodeList inLst;

            inLst = el.getElementsByTagName("username");
            tmp_el = (Element) inLst.item(0);
            this.username = tmp_el.getTextContent();
            inLst = el.getElementsByTagName("password");
            inLst = el.getElementsByTagName("name");
            tmp_el = (Element) inLst.item(0);
            name = tmp_el.getTextContent();
            inLst = el.getElementsByTagName("surname");
            tmp_el = (Element) inLst.item(0);
            surname = tmp_el.getTextContent();/**/

            inLst = r.getElementsByTagName("contact");
            
            for(int i = 0; i <inLst.getLength(); i++)
            {
                tmp_el = (Element)inLst.item(i);
                Contact co = new Contact(tmp_el.getAttribute("username"), tmp_el.getAttribute("name"), tmp_el.getAttribute("surname"));
                co.setState(tmp_el.getAttribute("state"));
                c.addContact(co);
            }
        }


    }

    /**
     * zkouma jestli je klient prihlasen
     * @return
     */
    public boolean is_logged() {
        return logged;
    }

    /**
     * gettery na prihlasovaci udaje
     * @return
     */
    public String getUsername() {
        return username;
    }

    public String getName() {
        return name;
    }

    public String getSurname() {
        return surname;
    }


    /**
     * pokusi se registrovat uzivatele podle zadanych udaju.
     *
     * @param un uzivatelske jmeno (unikatni!)
     * @param pw heslo
     * @param na jmeno
     * @param sn prijmeni
     * @throws Exception
     */
    public void register(String un, String pw, String na, String sn) throws Exception {
        TCPConnection c = TCPConnection.getInstance();
        String response = c.send("<msg type='register' username='" + un + "' password='" + pw + "' name='" + na + "' surname='" + sn + "' />");
        InputSource is = new InputSource();
        is.setCharacterStream(new StringReader(response));
        
        Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
        String state = node.getAttribute("state");
        if (state.equals("failed")) throw new Exception(node.getAttribute("error"));
    }

}
