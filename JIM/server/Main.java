/**
 * main trida serveru, jen spousti TCPConnection
 */

package JIM.server;

/**
 *
 * @author cypher
 */
public class Main {
    public static void main(String[] args) {
        try
        {
            TCPConnection.main(args);
        }catch(Exception e)
        {
            e.printStackTrace();
        }

    }
}
