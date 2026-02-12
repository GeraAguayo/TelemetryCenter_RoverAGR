import java.io.IOException;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;
import javax.swing.JOptionPane;

public class UDP {

    private DatagramSocket socket;
    private InetAddress ip;
    int PORT = 20001;
    //Sensor values
    float temp = 0.0f;
    float pres = 0.0f;
    float alt = 0.0f;
    float hum = 0.0f;
    float gas = 0.0f;
    float lat = 0.0f;
    float lon = 0.0f;
    //Current logs
    //public String LOG_TXT = "";
    //static int MAX_LOG_DISPLAY = 5;
    //Queue<String> log_queue = new ArrayDeque<>(MAX_LOG_DISPLAY);
    Syslog syslog_dict = new Syslog();
    //Control creation of multiple sockets
    boolean SOCKET_OPEN = false;
    int DISCONNECTION_LOG = 0;
    int CONNECTION_LOG = 1;

    //Constructor
    public UDP(String ip_address) throws IOException {
        connect(ip_address);
    }

    //Create and connect socket
    void connect(String ip_address) throws IOException {
        Scanner sc = new Scanner(System.in);
        this.ip = InetAddress.getByName(ip_address);
        close();
        //create socket with 8 segs of timeout
        this.socket = new DatagramSocket(PORT);
        this.socket.setSoTimeout(8000);
        this.SOCKET_OPEN = true;
        updateLog(CONNECTION_LOG);

    }

    //Close the connection
    public void close(){
        if (socket != null && !socket.isClosed()){
            socket.close();
            SOCKET_OPEN = false;
            updateLog(DISCONNECTION_LOG);
        }
    }

    //Update System logs queue
    void updateLog(int log_id){
        

        //check for size
        if (MainWindow.log_queue.size() >= MainWindow.MAX_LOG_DISPLAY){
            MainWindow.log_queue.poll();
        }

        // Get the current date and time
        Date now = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        String dateString = dateFormat.format(now);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm.ss a");
        String timeString = timeFormat.format(now);

        String new_msg = syslog_dict.getMessage(log_id) +" - " + dateString + " "  + timeString + " \n";
        MainWindow.log_queue.add(new_msg);
    }

    //Retrieve data
    public int retrieveData() throws IOException {
        try{

            //Send initial request
            String requestMsg = "Telemetry_Center";
            byte[] sendBuffer = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, ip, PORT);
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket;
            String receivedData;

            try{
                socket.send(sendPacket);
                //Prepare to receive data
                receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);
                //Get data from the rover
                socket.receive(receivePacket);
                receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());

            } catch (java.net.SocketTimeoutException e){
                //Connection lost
                System.out.println(e.getMessage());
                updateLog(DISCONNECTION_LOG);
                return -1;
            }

            //Sensor data received
            if (receivedData.equals("START_TM")){
                //Get the number of sensor values
                try{
                    socket.receive(receivePacket);
                    receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                } catch (java.net.SocketTimeoutException e){
                    //Connection lost
                    System.out.println("Connection lost: " + e.getMessage());
                    updateLog(DISCONNECTION_LOG);
                    return -1;
                }
                int n_values = Integer.parseInt(receivedData);
                float[] sensor_data = new float[n_values];
                Arrays.fill(sensor_data, Float.NaN);

                //Get sensor values
                //float[] sensor_data = {Float.NaN, Float.NaN, Float.NaN}; //Temp, press, alt
                for (int i = 0; i < n_values; i++){
                    try{
                        socket.receive(receivePacket);
                        receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                        try{
                            float sensor_val = Float.parseFloat(receivedData);
                            sensor_data[i] = sensor_val;
                        }
                        catch (Exception e){
                            System.out.println("Could not convert value! " + e);
                        }
                    } catch (java.net.SocketTimeoutException e){
                        //Connection lost
                        System.out.println(e.getMessage());
                        updateLog(DISCONNECTION_LOG);
                        return -1;
                    }

                }
                //Update sensor values
                this.temp = sensor_data[0];
                this.pres = sensor_data[1];
                this.alt = sensor_data[2];
                this.hum = sensor_data[3];
                this.gas = sensor_data[4];
                this.lat = sensor_data[5];
                this.lon = sensor_data[6];

                //Get end message
                try{
                    socket.receive(receivePacket);
                    receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                } catch (java.net.SocketTimeoutException e){
                    //Connection lost
                    System.out.println(e.getMessage());
                    updateLog(DISCONNECTION_LOG);
                    return -1;
                }

                return 0;

            }
            //SYSLOG received
            else if (receivedData.equals("SYSLOG")){
                try{
                    socket.receive(receivePacket);
                    receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    int log_id = Integer.parseInt(receivedData);
                    //Update global variable
                    updateLog(log_id);
                } catch (java.net.SocketTimeoutException e){
                    //Connection lost
                    System.out.println(e.getMessage());
                    updateLog(DISCONNECTION_LOG);
                    return -1;
                }
                //Get end message
                try{
                    socket.receive(receivePacket);
                    receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                    System.out.println("End syslog");
                } catch (java.net.SocketTimeoutException e){
                    //Connection lost
                    System.out.println("Connection lost: " + e.getMessage());
                    updateLog(DISCONNECTION_LOG);
                    return -1;
                }

                return 0;
            }
            else if(receivedData.equals("END")){
                return 0;
            }
            else{
                System.out.println("Unknow server msg: "+ receivedData);
                return 1;
            }
        } catch (IOException e ){
            System.err.println("Could not connect to server:" + e);
            return -1;
        }

    }
}
