/**
 * Message trida, umoznuje praci se zpravami.
 */

package JIM.client;
import java.util.Calendar;
import java.text.SimpleDateFormat;

/**
 *
 * @author cypher
 */
public class Message {


    private String type = new String(); // typ (incoming | outgoing)
    private Contact recipient = null; //adresat
    private String content = new String(); //obsah
    private long timestamp; //timestamp
    private final String DATE_FORMAT = "HH:mm:ss"; //format data
    private SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);


    /**
     * konstruktor bez urceneho timestampu
     * @param content
     */
    public Message(String content)
    {
        this.content = content.replace("'", "\"").replace("\\n", "\n");
        Calendar cal = Calendar.getInstance();
        timestamp = cal.getTimeInMillis();
    }

    /**
     * konstruktor s urcenym timestampem
     * @param content
     * @param time
     */
    public Message(String content, long time)
    {
        this.content = content.replace("'", "\"").replace("\\n", "\n");;
        this.timestamp = time;
    }

    /**
     * ruzne gettery a settery
     * @param recipient
     */
    public void setRecipient(Contact recipient)
    {
        this.recipient = recipient;
    }

    public void setType(String type)
    {
        if(type.equals("out")) this.type = "outgoing";
        else this.type = "incoming";
    }

    public String getContent()
    {
        return content;
    }

    public Contact getRecipient()
    {
        return recipient;
    }

    public String getType()
    {
        return type;
    }

    /**
     * vraci jestli zprava je prichozi nebo odchozi
     * @return
     */
    public boolean isIncoming()
    {
        return type.equals("incoming");
    }

    /**
     * @example 12:64:01 <Josef Novák> Ahoj, jak se máš?
     * @example 13:-5:22 <Já> Hele jde to, akorát dělám na semestrálce...
     * @return String
     */
    @Override
    public String toString()
    {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        String time = sdf.format(cal.getTime());

        String str = new String();
        str += time+" <";

        if(isIncoming()) str += recipient.toString();
        else str+="Já";

        str += "> "+content;

        return str;
    }

    /**
     * vraci xml string pro odeslani
     * @return
     */
    public String toXMLString()
    {
        String ret = "<msg type='message' recipient='"+recipient.getUsername()+"' content='"+content.replace("\n", "\\n")+"' />";
        return ret;
    }

}
