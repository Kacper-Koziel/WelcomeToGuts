import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowFocusListener;
import java.awt.image.BufferedImage;

public class Main {
    private static final int COLS = 320;
    private static final int ROWS = 120;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::initAndShow);
    }

    private static void initAndShow() {
        JFrame frame = new JFrame("Welcome to Gut's");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setUndecorated(true);
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);

        BufferedImage screenImage = new BufferedImage(COLS, ROWS, BufferedImage.TYPE_INT_RGB);
        GameEngine engine = new GameEngine(screenImage);
        GamePanel panel = engine.getPanel();

        frame.add(panel);

        frame.addWindowFocusListener(new WindowFocusListener() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                engine.setWindowFocused(true);
            }

            @Override
            public void windowLostFocus(WindowEvent e) {
                engine.setWindowFocused(false);
            }
        });

        frame.setVisible(true);

        BufferedImage cursorImg = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
        Cursor blankCursor = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");
        frame.setCursor(blankCursor);

        panel.requestFocusInWindow();
        frame.requestFocus();

        engine.start(frame);
    }
}