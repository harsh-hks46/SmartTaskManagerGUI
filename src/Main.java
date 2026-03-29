import gui.MainFrame;
import javax.swing.SwingUtilities;
import com.formdev.flatlaf.FlatDarkLaf;

public class Main {
    public static void main(String[] args) {
// Entry point of SmartTask Manager application
        try {
            FlatDarkLaf.setup();
        } catch (Exception e) {
            System.out.println("Failed to initialize FlatLaf");
        }

        SwingUtilities.invokeLater(() -> new MainFrame());
    }
}
