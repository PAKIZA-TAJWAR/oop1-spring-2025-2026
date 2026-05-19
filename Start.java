package librarymanagementsystem;
import librarymanagementsystem.gui.BookGUI;
import javax.swing.SwingUtilities;

public class Start {

    
    
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(BookGUI::new);
    }
}