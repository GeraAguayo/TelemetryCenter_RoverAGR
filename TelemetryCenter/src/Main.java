import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        MainWindow app = new MainWindow();

        SwingUtilities.invokeLater(() -> {
            app.setVisible(true);
        });
    }
}
