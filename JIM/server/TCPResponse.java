/**
 * objekt TCP odpovedi
 */

package JIM.server;
import java.util.Calendar;

/**
 *
 * @author cypher
 */
public class TCPResponse {
    private String recipient = null; //prijemce
    private String content = new String(); //obsah
    private String attributes = new String(); //atributy
    private String type = new String(); //typ

    /**
     * vytvarime podle recipienta a typu
     * @param r
     * @param t
     */
    public TCPResponse(String r, String t)
    {
        recipient = r;
        type = t;
        addAttribute("type", type);
        
        Calendar cal = Calendar.getInstance();
        Long time = cal.getTimeInMillis();
        addAttribute("timestamp", time.toString());

    }

    /**
     * ruzne gettery a settery na obsah
     * @param c
     */
    public void setContent(String c)
    {
        content = c;
    }

    public String getType()
    {
        return type;
    }

    public String getContent()
    {
        return content;
    }

    public String getRecipient()
    {
        return recipient;
    }

    /**
     * pridavame atribut
     * @param name
     * @param value
     */
    public void addAttribute(String name, String value)
    {
        attributes += " "+name+"='"+value+"'";
    }

    @Override
    /**
     * vracime xml zpravu
     * @example <msg type='message' recipient='zupanic'>BLA BLA BLA</msg>
     */
    public String toString()
    {
        return "<msg"+attributes+">"+content+"</msg>";
    }
}
