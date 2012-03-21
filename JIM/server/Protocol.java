/**
 * protokol TCP prenosu
 */
package JIM.server;
import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Element;
import java.util.ArrayList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;



/**
 *
 * @author cypher
 */
public class Protocol {
    /**
     * zpracovava prichozi request
     * @param request
     * @return
     */
    public static TCPResponse handle(String request)
    {
        String type, recipient;
        TCPResponse response = null;
        try{
            
            InputSource is = new InputSource();
            is.setCharacterStream(new StringReader(request));
            Element node =  DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is).getDocumentElement();
            type = node.getAttribute("type");
            recipient = node.getAttribute("recipient");

            // přihlašování
            if(type.equals("login"))
            {
                String username, password;
                username = node.getAttribute("username");
                password = node.getAttribute("password");
                XMLClientsIO x = XMLClientsIO.getInstance();
                ClientNode cl = x.findUserPassword(username, password);
                response = new TCPResponse(recipient, "login");
                if(cl == null) response.addAttribute("state", "failed");
                else
                {
                    response.addAttribute("state", "ok");
                    response.setContent(cl.toString());
                    TCPConnection.stateChangeNotify(username, "online");

                }
            }

            //odhlasovani
            if(type.equals("signoff"))
            {
                response = new TCPResponse(recipient, "signoff");
                response.addAttribute("state", "ok");
            }

            //registrace
            if(type.equals("register"))
            {
                String un, pw, na, surna;
                un = node.getAttribute("username");
                pw = node.getAttribute("password");
                na = node.getAttribute("name");
                surna = node.getAttribute("surname");
                response = new TCPResponse(recipient, "register");

                try
                {
                    if(pw.length() < 6) throw new IllegalArgumentException("Minimální délka hesla je 6 znaků.");
                    if(un.length() < 4) throw new IllegalArgumentException("Minimální délka uživatelského jména je 4 znaky.");
                    XMLClientsIO i = XMLClientsIO.getInstance();
                    i.addClient(new ClientNode(un, pw, na, surna));
                    response.addAttribute("state", "ok");
                }catch(Exception e)
                {
                    response.addAttribute("state", "failed");
                    response.addAttribute("error", e.getMessage());
                }
                
            }

            //zprava
            if(type.equals("message"))
            {
                String content = node.getAttribute("content");
                response = new TCPResponse(recipient, "message");
                response.addAttribute("content", content);
               
            }

            //vyhledavani
            if(type.equals("search"))
            {
                String query = node.getAttribute("query");
                response = new TCPResponse(recipient, "search");
                XMLClientsIO i = XMLClientsIO.getInstance();
                ArrayList<ClientNode> contacts = i.findContact(query, recipient);
                String content = new String();
                for(int x = 0; x < contacts.size(); x++) content += contacts.get(x).getAsContact();
                response.setContent(content);
            }

            //pridani kontaktu
            if(type.equals("contact-added"))
            {
                String from = node.getAttribute("from");
                XMLClientsIO x = XMLClientsIO.getInstance();
                ClientNode contact_from = x.getByUsername(from);
                ClientNode contact_recipient = x.getByUsername(recipient);
                x.addContact(contact_from, contact_recipient);
                response = new TCPResponse(recipient, "contact-added");
                response.setContent(contact_from.getAsContact());
            }

            //odstraneni kontaktu
            if(type.equals("contact-removed"))
            {
                String from = node.getAttribute("from");
                XMLClientsIO x = XMLClientsIO.getInstance();
                ClientNode contact_from = x.getByUsername(from);
                ClientNode contact_recipient = x.getByUsername(recipient);
                x.removeContact(contact_from, contact_recipient);
                response = new TCPResponse(recipient, "contact-removed");
                response.addAttribute("username", contact_from.getUsername());
            }
            
        }catch(IOException e){e.printStackTrace();}/**/
        catch(SAXException e){e.printStackTrace();}
        catch(ParserConfigurationException e){e.printStackTrace();}
        return response;
    }



}
