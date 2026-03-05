import javax.swing.*;

public class TelemetryDisplay extends JFrame {

    private JPanel MainPanel;

    public TelemetryDisplay(){
        this.setTitle("Telemetry AGR");
        //this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("src/media/logo_black.png")));
        this.setContentPane(MainPanel);
        this.pack();
        this.setSize(1920,1080);
    }
}
