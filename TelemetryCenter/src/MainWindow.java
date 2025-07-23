import javax.swing.*;

public class MainWindow extends JFrame {
    private JPanel MainPanel;
    private JPanel section1;
    private JPanel section2;
    private JPanel section3;
    private JPanel datetimePanel;
    private JPanel logoPanel;
    private JPanel videoPanel;
    private JPanel sensorPanel;
    private JPanel chartPanel;
    private JPanel logsPanel;
    private JLabel datetimeTitle;
    private JLabel logoTitle;
    private JLabel videoTitle;
    private JLabel sensorTitle;
    private JLabel chartTitle;
    private JLabel logsTitle;

    public MainWindow(){
        this.setTitle("Telemetry AGR");
        //this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("Imagenes/logo.png")));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(MainPanel);
        this.pack();
        this.setSize(1920,1080);
        this.setLocationRelativeTo(null);
    }
}
