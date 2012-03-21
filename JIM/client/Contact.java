/**
 * Contact třída, umožňuje práci s kontakty
 */

package JIM.client;

/**
 *
 * @author cypher
 */
public class Contact {
    private String name = new String(); //jmeno
    private String surname = new String(); //prijmeni
    private String username = new String(); //uzivatelske jmeno
    private boolean online = false; //online status

    /**
     * konstruktor, vytvari kontakt z jeho udaju
     * @param username
     * @param name
     * @param surname
     */
    public Contact(String username, String name, String surname)
    {
        this.username = username;
        this.surname = surname;
        this.name = name;
        
    }

    /**
     * nstavuje stav
     * @param state string (online | offline)
     */
    public void setState(String state)
    {
        if(state.equals("online")) online = true;
        if(state.equals("offline")) online = false;
    }

    /**
     * vraci zda je kontakt online
     * @return
     */
    public boolean isOnline()
    {
        return online;
    }

    /**
     * getter na jmeno kontaktu
     * @return
     */
    public String getName()
    {
        return name;
    }

    /**
     * getter na uzivatelske jmeno kontaktu
     * @return
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * vraci (<Jmeno> <Prijmeni>)
     * @return
     */
    @Override
    public String toString()
    {
        return name+" "+surname;
    }

    @Override
    public boolean equals(Object o)
    {
        boolean equals = false;
        if(o instanceof Contact)
        {
            Contact c = (Contact)o;
            if(username.equals(c.getUsername())) equals = true;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 83 * hash + (this.username != null ? this.username.hashCode() : 0);
        return hash;
    }
}
