import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Queue;
import java.util.Stack;

public class MainWindow extends JFrame {
    private JPanel MainPanel;
    private JLabel datetimeLabel;
    private JLabel logoLabel;
    private JPanel videoPanel;
    private JPanel sensorPanel;
    private JPanel logPanel;
    private JPanel addonPanel;
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
    private JLabel green_light;
    private JLabel yellow_light;
    private JLabel red_light;
    private JButton stop_btn;
    private JLabel titlePosition;
    private JLabel labelHum;
    private JLabel labelGas;
    private JLabel titleHum;
    private JLabel titleGas;
    private JLabel labelLat;
    private JLabel labelLon;
    public JLabel videoLabel;
    private JLabel distanceTotal;
    private JLabel labelDistance;
    private JLabel deltaTitle;
    private JButton resetTravelBtn;
    private JButton graphBtn;
    private JButton startWritingBtn;
    private JButton stopWritingBtn;
    private JLabel addonTitle;
    private JLabel labelDelta;

    //Udp mgmt
    UDP udp_client;
    boolean udp_ready = false;
    String ip_address = "";

    //Timers
    Timer telemetryTimer;
    Timer guiTimer;

    //Syslog manager
    String LOG_TXT = "";
    static int MAX_LOG_DISPLAY = 5;
    public static Queue<String> log_queue = new ArrayDeque<>(MAX_LOG_DISPLAY);

    //GPS Calculations
    DistanceCalculation coords_calculator = new DistanceCalculation();

    //Telemetry management
    TelemetryWriter telemetryManager = new TelemetryWriter();

    //Chart manager
//    LineChart tempChartObj = new LineChart();
//    int MAX_CHART_ELEMENTS = 10;
//    Queue<Float> temp_values = new ArrayDeque<>(MAX_CHART_ELEMENTS);


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

        startUpdaterGUI();

        //Listener for btn ip set (connect)
        buttonIP.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                on_yellow();
                ip_address = inputIP.getText();
                if (ip_address.isBlank()){
                    JOptionPane.showMessageDialog(null, "Set an IP address","IP Error",JOptionPane.ERROR_MESSAGE);
                }
                else{
                    //Instantiate UDP class
                    try {
                        if (udp_client != null){
                            //If active socket, close it
                            udp_client.close();
                        }
                        udp_client = new UDP(ip_address);
                        udp_ready = true;
                        startUpdaterTelemetry();

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
                //Stop Telemetry & GUI timers
                if (telemetryTimer != null){
                    telemetryTimer.stop();
                }
                if (guiTimer != null){
                    guiTimer.stop();
                }
                System.exit(0);
            }
        });

        //Listener for btn STOP
        stop_btn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                udp_client.close();
                udp_ready = false;
                //Stop Telemetry timer
                if (telemetryTimer != null){
                    telemetryTimer.stop();
                }
                on_red();
                setLabelSensorNA();
            }
        });

        //Listener for btn reset
        btnRetryConect.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                //Close current connection and telemetry timer
                on_red();
                coords_calculator.distance_traveled = 0;
                setLabelSensorNA();
                udp_client.close();
                udp_ready = false;
                if (telemetryTimer != null){
                    telemetryTimer.stop();
                }
                //Start new connection with same IP address
                on_yellow();
                try {
                    udp_client = new UDP(ip_address);
                    udp_ready = true;
                    startUpdaterTelemetry();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
                    on_red();
                    udp_ready = false;
                    udp_client.close();
                }
            }
        });

        //Listener for reset travel btn
        resetTravelBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                coords_calculator.distance_traveled = 0;

            }
        });

        //Listener for start writing btn
        startWritingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startWritingBtn.setEnabled(false);
                stopWritingBtn.setEnabled(true);
                telemetryManager.startFile();


            }
        });

        //Listener for stop writing btn
        stopWritingBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                startWritingBtn.setEnabled(true);
                stopWritingBtn.setEnabled(false);
                telemetryManager.endFile();

            }
        });
    }

    private void updateGUIValues(){
        //Update Date and time string
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String timeString = timeFormat.format(now);
        this.datetimeLabel.setText("Date: " + dateString + "   Time: " + timeString);

    }

    private void updateSensorValues() throws IOException {
        try{
            //Update sensor values based on UDP client info
            String temp_str = String.valueOf(udp_client.temp);
            String alt_str = String.valueOf(udp_client.alt);
            String pres_str = String.valueOf(udp_client.pres);
            String hum_str = String.valueOf(udp_client.hum);
            String gas_str = String.valueOf(udp_client.gas);
            String lat_str = String.valueOf(udp_client.lat);
            String lon_str = String.valueOf(udp_client.lon);
            int return_code = udp_client.retrieveData();
            this.labelTemp.setText(temp_str);
            this.labelAlt.setText(alt_str);
            this.labelPress.setText(pres_str);
            this.labelHum.setText(hum_str);
            this.labelGas.setText(gas_str);
            this.labelLat.setText("Lat: " + lat_str);
            this.labelLon.setText("Lon: " + lon_str);
            //Calculate delta and distance traveled
            coords_calculator.addCoordinates(udp_client.lat, udp_client.lon);
            double delta = coords_calculator.calculateDelta();
            this.labelDelta.setText(String.format("%.2f m", delta));
            this.labelDistance.setText(String.format("%.2f m", coords_calculator.distance_traveled));
            //Update LOGS
            renderLogs();
            //Write data to log
            LocalDateTime myDateObj = LocalDateTime.now();
            DateTimeFormatter date_f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH:mm");
            String date_str = myDateObj.format(date_f);
            String time_str = myDateObj.format(time_f);
            String[] telemetry_row = {
                    date_str,
                    time_str,
                    temp_str,
                    alt_str,
                    pres_str,
                    hum_str,
                    gas_str,
                    lat_str,
                    lon_str,
                    String.valueOf(coords_calculator.distance_traveled)
            };
            telemetryManager.logTelemetry(telemetry_row);

            //Update connection status lights
            switch (return_code){
                case 0:
                    //Normal return
                    on_green();
                    break;
                case -1:
                    //Error in connection
                    on_red();
                    setLabelSensorNA();
                    udp_client.close();
                    break;
            }
        }catch (IOException ex){
            if (udp_client != null) {
                // Close the socket on connection loss
                udp_client.close();
            }
            this.udp_ready = false;
            on_red();
            JOptionPane.showMessageDialog(null, ex.getMessage(), "Connection lost!", JOptionPane.ERROR_MESSAGE);
        }
    }

    //Timer for telemetry updates
    private void startUpdaterTelemetry() {
        this.telemetryTimer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateSensorValues();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.telemetryTimer.setInitialDelay(0);
        this.telemetryTimer.start();
    }

    //Timer for GUI updates
    private void startUpdaterGUI(){
        this.guiTimer = new Timer(5000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    updateGUIValues();
                } catch (Exception ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        this.guiTimer.setInitialDelay(0);
        this.guiTimer.start();

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

    private void setLabelSensorNA(){
        //Sets the value of the sensor values to N/A on disconnection
        this.labelTemp.setText("N/A");
        this.labelAlt.setText("N/A");
        this.labelPress.setText("N/A");
        this.labelHum.setText("N/A");
        this.labelGas.setText("N/A");
        this.labelLat.setText("N/A");
        this.labelLon.setText("N/A");
    }

    //Convert from log ids to definitions
    private void renderLogs(){
        LOG_TXT = "";
        Stack<String> logs_display_order = new Stack<>();
        for (String msg : log_queue){
            logs_display_order.push(msg);
        }

        while (!logs_display_order.isEmpty()){
            LOG_TXT += logs_display_order.pop();
        }
        this.syslogTextArea.setText(LOG_TXT);
    }

    //Create Line chart for temp
//    void createTemperatureChart(){
//        JFreeChart tempChart = tempChartObj.createChartFloat(
//                "Temperature",
//                "",
//                "C°",
//                "Temperature Values",
//                temp_values
//        );
//        ChartPanel chartPanel = new ChartPanel(tempChart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(400, 200));
//        addonPanel.add(chartPanel);
//    }
//
//    void updateTemperatureChart(){
//        int key = 1;
//        for (float val : temp_values){
//            tempChartObj.dataset.addValue(val, "Temp values","T_"+key);
//            key++;
//        }
//    }
}
