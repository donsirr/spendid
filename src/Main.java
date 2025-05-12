import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import com.formdev.flatlaf.themes.FlatMacDarkLaf;
import java.awt.Font;
import java.io.File;
import java.awt.GraphicsEnvironment;

public class Main {
    public static void main(String[] args) {
        try {

            // Set Theme
            UIManager.setLookAndFeel(new FlatMacDarkLaf());

            // Load and Register Font
            Font sfPro = Font.createFont(Font.TRUETYPE_FONT, new File("src/fonts/SF-Pro-Display-Medium.ttf")).deriveFont(14f);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(sfPro);

            // Apply it as default font
            UIManager.put("defaultFont", sfPro);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Launch
        SwingUtilities.invokeLater(() -> new view.MainView());
    }
}
