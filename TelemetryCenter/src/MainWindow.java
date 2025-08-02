import javax.swing.*;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainWindow extends JFrame {
    private JPanel MainPanel;
    private JLabel datetimeLabel;
    private JLabel logoLabel;
    private JPanel videoPanel;
    private JPanel sensorPanel;
    private JPanel chartPanel;
    private JPanel logsPanel;
    private JLabel titleSensor;
    private JLabel labelTitleTemp;
    private JLabel labelTitleAlt;
    private JLabel labelTitlePres;
    private JLabel labelTemp;
    private JLabel labelAlt;
    private JLabel labelPress;
    private JTextField inputIP;
    private JButton buttonIP;
    private JLabel videoTitle;
    private JButton btnRetryConect;
    private JLabel titleLogs;
    private JTextArea syslogTextArea;
    private JButton primaryChecksBtn;

    UDP udp_client;
    boolean udp_ready = false;

    public MainWindow(){
        this.setTitle("Telemetry AGR");
        //this.setIconImage(Toolkit.getDefaultToolkit().getImage(ClassLoader.getSystemResource("src/media/logo_black.png")));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setContentPane(MainPanel);
        this.pack();
        this.setSize(1920,1080);
        this.setLocationRelativeTo(null);

        //Initialize datetime timer
        startUpdater();
        //Listener for btn ip set
        buttonIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String in_addr = inputIP.getText();
                if (in_addr.isBlank()){
                    JOptionPane.showMessageDialog(null, "Set an IP address");
                }
                else{
                    //Instantiate UDP class
                    try {
                        udp_client = new UDP(in_addr);
                        udp_ready = true;
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
    }

    private void updateInterface() throws IOException {
        //Update Date and time string
        Date now = new Date(); // Get the current date and time
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String timeString = timeFormat.format(now);
        this.datetimeLabel.setText("Date: " + dateString + "   Time: " + timeString);

        //Update sensor values
        if (udp_ready){
            updateSensorValues();
        }
    }

    private void updateSensorValues() throws IOException {
        //Update sensor values based on UDP client info
        udp_client.retrieveData();
        this.labelTemp.setText(String.valueOf(udp_client.temp));
        this.labelAlt.setText(String.valueOf(udp_client.alt));
        this.labelPress.setText(String.valueOf(udp_client.pres));
        this.syslogTextArea.setText(udp_client.LOG_TXT);

    }

    private void startUpdater() {
        Timer timer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateInterface();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        timer.setInitialDelay(0);
        timer.start();
    }

}
