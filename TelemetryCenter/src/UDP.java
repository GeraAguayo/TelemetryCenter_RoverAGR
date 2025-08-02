import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class UDP {

    private DatagramSocket socket;
    private InetAddress ip;
    int PORT = 20001;
    //Sensor values
    float temp = 0.0f;
    float pres = 0.0f;
    float alt = 0.0f;
    //Current logs
    public String LOG_TXT = "";
    static int MAX_LOG_DISPLAY = 5;
    Queue<String> log_queue = new ArrayDeque<>(MAX_LOG_DISPLAY);
    Syslog syslog_dict = new Syslog();

    //Constructor
    public UDP(String ip_address) throws IOException {
        Scanner sc = new Scanner(System.in);
        ip = InetAddress.getByName(ip_address);
        //create socket
        socket = new DatagramSocket(PORT);
    }

    //Manage logs
    void updateLog(int log_id){
        LOG_TXT = "";

        //check for size
        if (log_queue.size() >= MAX_LOG_DISPLAY){
            log_queue.poll();
        }

        // Get the current date and time
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a");
        String timeString = timeFormat.format(now);

        String new_msg = syslog_dict.getMessage(log_id) +" - " + dateString + " "  + timeString + " \n";
        log_queue.add(new_msg);

        for (String msg : log_queue){
            LOG_TXT += msg;
        }

    }


    //Retrieve data
    public int retrieveData() throws IOException {

        while(true){
            //send request
            String requestMsg = "Telemetry_Center";
            byte[] sendBuffer = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, ip, PORT);
            System.out.println("Sending request to rover...");
            socket.send(sendPacket);

            //Prepare to receive data
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            //Get data from the rover
            socket.receive(receivePacket);
            String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
            System.out.println("A: " + receivedData);

            //Sensor data received
            if (receivedData.equals("START_TM")){
                System.out.println("Sensor data received");
                //Get the number of sensor values
                socket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("B: " + receivedData);
                int n_values = Integer.parseInt(receivedData);
                //Get sensor values
                float[] sensor_data = {Float.NaN, Float.NaN, Float.NaN}; //Temp, press, alt
                for (int i = 0; i < n_values; i++){
                    socket.receive(receivePacket);
                    receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("C: " + receivedData);
                    try{
                        float sensor_val = Float.parseFloat(receivedData);
                        sensor_data[i] = sensor_val;
                    }
                    catch (Exception e){
                        System.out.println("Could not convert value!");
                        System.out.println(e);
                    }
                }
                //Update sensor values
                this.temp = sensor_data[0];
                this.pres = sensor_data[1];
                this.alt = sensor_data[2];

                //Get end message
                socket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("End sensor data " + receivedData);
                return 0;


            }
            //SYSLOG received
            else if (receivedData.equals("SYSLOG")){
                System.out.println("SYSLOG Received");
                socket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                int log_id = Integer.parseInt(receivedData);

                //Update global variable
                updateLog(log_id);

                //Get end message
                socket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("End syslog " + receivedData);
                return 0;
            }
            else if(receivedData.equals("END")){
                System.out.println("End of packet");
                return 0;

            }
            else{
                System.out.println("Unknow server msg: "+ receivedData);
                return 1;
            }

        }

    }

}
