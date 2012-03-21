/**
 * renderer seznamu kontaktů, umožňuje zabarvení podle stavu
 */
package JIM.client;
import java.awt.Component;

import javax.swing.DefaultListCellRenderer;;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Color;
/**
 *
 * @author cypher
 */
class ContactsRenderer implements ListCellRenderer {
  protected DefaultListCellRenderer defaultRenderer = new DefaultListCellRenderer();

  public Component getListCellRendererComponent(JList list, Object value, int index,
      boolean isSelected, boolean cellHasFocus) {


        JLabel renderer = (JLabel) defaultRenderer.getListCellRendererComponent(list, value, index,
            isSelected, cellHasFocus);

    
        Contact val = (Contact)value;
        
        renderer.setForeground((val.isOnline()) ? Color.GREEN : Color.RED);
    return renderer;
  }
}
