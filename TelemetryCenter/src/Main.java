import javax.swing.*;
import java.awt.image.BufferedImage;

public class Main {
    public static void main(String[] args) {
        VideoReceiver receiver = new VideoReceiver();
        receiver.startServer(5000);
        MainWindow app = new MainWindow();

        SwingUtilities.invokeLater(() -> {
            app.setVisible(true);
        });

        //video streaming
        javax.swing.Timer timer = new javax.swing.Timer(50, e -> {
            BufferedImage img = receiver.getLatestFrame();
            if (img != null) {
                app.videoLabel.setIcon(new ImageIcon(img));
            }
        });
        timer.start();
        //video streaming
    }
}
