import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.BindException;
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
    private JLabel green_light;
    private JLabel yellow_light;
    private JLabel red_light;

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
        this.green_light.setEnabled(false);
        this.yellow_light.setEnabled(false);
        this.red_light.setEnabled(false);

        //Initialize datetime timer
        startUpdater();

        //Listener for btn ip set
        buttonIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String in_addr = inputIP.getText();
                if (in_addr.isBlank()){
                    JOptionPane.showMessageDialog(null, "Set an IP address","IP Error",JOptionPane.ERROR_MESSAGE);
                }
                else{
                    //Instantiate UDP class
                    try {
                        if (udp_client != null){
                            //If active socket, close it
                            System.out.println("There was a udp client instance created");
                            udp_client.close();
                        }
                        udp_client = new UDP(in_addr);
                        udp_ready = true;
                        on_yellow();
                    } catch (IOException ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                        on_red();
                        udp_ready = false;
                        udp_client.close();
                    }
                }
            }
        });

        //Listener for window exit
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (udp_ready && udp_client != null) {
                    udp_client.close();
                }
                System.exit(0);
            }
        });
    }

    private void updateInterface() throws IOException {
        //Update Date and time string
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String timeString = timeFormat.format(now);
        this.datetimeLabel.setText("Date: " + dateString + "   Time: " + timeString);

        //Update sensor values
        if (udp_ready){
            try{
                updateSensorValues();
            }catch (IOException ex){
                if (udp_client != null) {
                    // Close the socket on connection loss
                    udp_client.close();
                }
                udp_ready = false;
                on_red();
                JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection lost!", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void updateSensorValues() throws IOException {
        //Update sensor values based on UDP client info
        int return_code = udp_client.retrieveData();
        this.labelTemp.setText(String.valueOf(udp_client.temp));
        this.labelAlt.setText(String.valueOf(udp_client.alt));
        this.labelPress.setText(String.valueOf(udp_client.pres));
        this.syslogTextArea.setText(udp_client.LOG_TXT);

        //Update connection status lights
        switch (return_code){
            case 0:
                //Normal return
                on_green();
                break;
            case -1:
                //Error in connection
                on_red();
                udp_client.close();
                break;
        }

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
    //Functions to control light indicator
    private void on_green(){
        this.green_light.setEnabled(true);
        this.yellow_light.setEnabled(false);
        this.red_light.setEnabled(false);
    }

    private void on_yellow(){
        this.green_light.setEnabled(false);
        this.yellow_light.setEnabled(true);
        this.red_light.setEnabled(false);
    }

    private void on_red(){
        this.green_light.setEnabled(false);
        this.yellow_light.setEnabled(false);
        this.red_light.setEnabled(true);
    }


}
