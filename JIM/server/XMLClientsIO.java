/**
 * prace s databazi klientu (xml soubor v adresari zdrojovych kodu balicku server)
 */

package JIM.server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.*;
import javax.xml.transform.stream.*;
import javax.xml.transform.*;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.util.ArrayList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 *
 * @author cypher
 */
public class XMLClientsIO {

    private static XMLClientsIO instance = null;
    private ArrayList<ClientNode> clients = new ArrayList<ClientNode>();
    private Document d = null;
    private File file = null;
    private Node root = null;

    /**
     * singleton
     */
    private XMLClientsIO() {
        readFromFile();
    }

    public static XMLClientsIO getInstance() {
        if (!(instance instanceof XMLClientsIO)) {
            instance = new XMLClientsIO();
        }
        return instance;
    }

    /**
     * nacte ze souboru, vytbori objekty klientu a jejich kontakty
     */
    private void readFromFile() {
        try {
            file = new File("src/pr2_semestralka/server/clients.xml");
            DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = df.newDocumentBuilder();

            d = db.parse(file);
            root = d.getDocumentElement();
            root.normalize();


            NodeList lst = d.getElementsByTagName("client");


            for (int i = 0; i < lst.getLength(); i++) {
                Node node = lst.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    //Element el = (Element) node;
                    Element el = (Element) node;
                    Element tmp_el;

                    NodeList inLst;

                    String username, password, name, surname;
                    inLst = el.getElementsByTagName("username");
                    tmp_el = (Element) inLst.item(0);
                    username = tmp_el.getTextContent();
                    inLst = el.getElementsByTagName("password");
                    tmp_el = (Element) inLst.item(0);
                    password = tmp_el.getTextContent();
                    inLst = el.getElementsByTagName("name");
                    tmp_el = (Element) inLst.item(0);
                    name = tmp_el.getTextContent();
                    inLst = el.getElementsByTagName("surname");
                    tmp_el = (Element) inLst.item(0);
                    surname = tmp_el.getTextContent();
                    inLst = el.getElementsByTagName("contact");

                    ClientNode n = new ClientNode(username, password, name, surname);

                    for (int y = 0; y < inLst.getLength(); y++) {
                        tmp_el = (Element) inLst.item(y);

                        try {
                            n.addContact(tmp_el.getAttribute("username"), tmp_el.getAttribute("name"), tmp_el.getAttribute("surname"));
                        } catch (Exception e) {
                            System.out.println("Nepodařilo se přidat kontakt.");
                        }
                    }



                    clients.add(n);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * hleda kombinaci jmena a hesla (pro prihlasovani)
     * @param username
     * @param password
     * @return
     */
    public ClientNode findUserPassword(String username, String password) {
        username = username.toLowerCase();

        ClientNode returnNode = null;

        int i = 0;

        while (i < clients.size()) {
            ClientNode client = clients.get(i);
            String un = client.getUsername().toLowerCase();
            String pw = client.getPassword();
            if (un.equals(username) && pw.equals(password)) {
                returnNode = client;
                break;
            }

            i++;
        }
        return returnNode;
    }

    /**
     * zapisuje zpatky do souboru
     */
    private void write() {
        try {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(d);
            FileWriter f = new FileWriter(file);
            StreamResult result = new StreamResult(f);
            transformer.transform(source, result);
        } catch (Exception e) {
            System.out.println("Writing XML file failed.");
        }
    }

    /**
     * pridava klienta (pokus o registraci)
     * @param client
     * @throws IllegalStateException pokud uzivatelske jmeno uz existuje (klient se nevytvori)
     */
    public void addClient(ClientNode client) throws IllegalStateException {
        try {
            if (usernameExists(client.getUsername())) {
                throw new IllegalStateException("Toto uživatelské jméno již existuje.");
            }
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(client.toString()));
            Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
            Node n = d.importNode(node, true);
            root.appendChild(n);
            clients.add(client);
        } catch (IOException e) {
            e.printStackTrace();
        }/**/ catch (SAXException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        write();

    }

    /**
     * vraci klienta podle uzivatelskeho jmena
     * @param username
     * @return
     */
    public ClientNode getByUsername(String username) {
        int i = 0;
        ClientNode c = null;

        while (i < clients.size()) {
            c = clients.get(i);
            if (c.getUsername().equals(username)) {
                break;
            }
            i++;
        }

        return c;
    }

    /**
     * pridava kontakt (vzajemne mezi c1 a c2)
     * @param c1
     * @param c2
     */
    public void addContact(ClientNode c1, ClientNode c2) {
        //napřed do nodů
        ClientNode c;
        for (int i = 0; i < clients.size(); i++) {
            c = clients.get(i);
            try {
                if (c.getUsername().equals(c1.getUsername())) {
                    c.addContact(c2.getUsername(), c2.getName(), c2.getSurname());
                }
                if (c.getUsername().equals(c2.getUsername())) {
                    c.addContact(c1.getUsername(), c1.getName(), c1.getSurname());
                }
            } catch (Exception e) {
            }

        }

        //potom do DOMu
        refreshClients();

        //a pak zapsat na disk
        write();
    }


    /**
     * odstranuje kontakt mezi c1 a c2
     * @param c1
     * @param c2
     */
    public void removeContact(ClientNode c1, ClientNode c2)
    {
        //napřed z nodů
        ClientNode c;
        for (int i = 0; i < clients.size(); i++) {
            c = clients.get(i);
            try {
                if (c.getUsername().equals(c1.getUsername())) {
                    c.removeContact(c2.getUsername());
                }
                if (c.getUsername().equals(c2.getUsername())) {
                    c.removeContact(c1.getUsername());
                }
            } catch (Exception e) {
            }

        }

        //potom z DOMu
        refreshClients();

        //a pak zapsat na disk
        write();
    }

    /**
     * synchronizuje klienty mezi stavajici instanci a xml souborem
     */
    public void refreshClients() {
        Element new_root = null;
        Document new_document = null;
        try {
            new_document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            new_root = new_document.createElement("clients");
            new_document.appendChild(new_root);
        } catch (Exception e) {}
        for (int i = 0; i < clients.size(); i++) {
            
            ClientNode client = clients.get(i);
            try {
                InputSource is = new InputSource();
                is.setCharacterStream(new StringReader(client.toString()));
                Element node = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
                Node n = new_document.importNode(node, true);
                new_root.appendChild(n);

            } catch (IOException e) {
                e.printStackTrace();
            }/**/ catch (SAXException e) {
                e.printStackTrace();
            } catch (ParserConfigurationException e) {
                e.printStackTrace();
            }
        }

        root = new_root;
        d = new_document;
        
    }

    /**
     * overuje jestli uzivatelske jmeno uz existuje
     * @param username
     * @return
     */
    public boolean usernameExists(String username) {
        username = username.toLowerCase();
        boolean exists = false;
        int i = 0;
        while (i < clients.size()) {
            if (username.equals(clients.get(i).getUsername().toLowerCase())) {
                exists = true;
                break;
            }
            i++;
        }

        return exists;
    }

    /**
     * hleda kontakt, fulltextove podle query stringu, vraci arraylist vysledku
     * @param query
     * @param whoAsks
     * @return
     */
    public ArrayList<ClientNode> findContact(String query, String whoAsks) {
        String regex = "([\\S|\\s]*?(" + query.replaceAll("\\s+$", "") + "){1,}[\\S|\\s]*?{0,}){1,}";
        Pattern pattern = Pattern.compile(regex, Pattern.UNICODE_CASE | Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);
        ArrayList<ClientNode> matches = new ArrayList<ClientNode>();
        Matcher m = null;

        int i = 0;
        String current = null;
        ClientNode cl;
        while (i < clients.size()) {
            cl = clients.get(i);
            if (!cl.getUsername().toLowerCase().equals(whoAsks.toLowerCase())) {
                current = cl.getName() + cl.getSurname() + cl.getUsername().replaceAll("\\s+$", "");
                m = pattern.matcher(current);
                if (m.matches()) {
                    matches.add(cl);
                }
            }
            i++;
        }

        return matches;

    }
}
