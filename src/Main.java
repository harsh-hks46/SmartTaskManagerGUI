import gui.MainFrame;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {

        // Set modern look and feel
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatLaf");
        }

        // Run GUI on Event Dispatch Thread (important for Swing)
        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}