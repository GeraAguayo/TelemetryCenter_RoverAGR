import java.io.IOException;
import java.net.*;
import java.util.Scanner;

public class UDP {

    private DatagramSocket socket;
    private InetAddress ip;
    int PORT = 20001;
    //Sensor values
    float temp = 0.0f;
    float pres = 0.0f;
    float alt = 0.0f;

    //Constructor
    public UDP(String ip_address) throws IOException {
        Scanner sc = new Scanner(System.in);
        ip = InetAddress.getByName(ip_address);
        //create socket
        socket = new DatagramSocket(PORT);
    }

    //Retrieve data
    public int retrieveData() throws IOException {
        while(true){
            //send request
            String requestMsg = "REQUEST_TM";
            byte[] sendBuffer = requestMsg.getBytes();
            DatagramPacket sendPacket = new DatagramPacket(sendBuffer, sendBuffer.length, ip, PORT);
            System.out.println("Sending request to rover...");
            socket.send(sendPacket);

            //Prepare to receive data
            byte[] receiveBuffer = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receiveBuffer, receiveBuffer.length);

            //Get data from the rover
            int count = -1;
            boolean finished = false;
            while(!finished){
                socket.receive(receivePacket);
                String receivedData = new String(receivePacket.getData(), 0, receivePacket.getLength());
                if (receivedData.equals("START_TM")){
                    System.out.println("Telemtry start");
                    count = 0;
                }
                else if(receivedData.equals("END_TM")){
                    finished = true;
                    System.out.println("End of telemetry");
                    System.out.println("---------------------------------------------");
                }
                else{
                    switch (count){
                        case 1:
                            //Temperature
                            float temp_val = 0.0f;
                            try{
                                temp_val = Float.parseFloat(receivedData);
                            } catch (Exception e){
                                temp_val = 0.0f;
                                continue;
                            }

                            this.temp = temp_val;
                            System.out.println("Temperature: ");
                            System.out.println(temp);
                            break;
                        case 2:
                            //Pressure
                            float pres_val = 0.0f;
                            try {
                                pres_val = Float.parseFloat(receivedData);
                            } catch (Exception e){
                                pres_val = 0.0f;
                                continue;
                            }

                            this.pres = pres_val;
                            System.out.println("Pressure:");
                            System.out.println(pres);
                            break;
                        case 3:
                            //Altitude
                            float alt_val = 0.0f;
                            try{
                                alt_val = Float.parseFloat(receivedData);
                            } catch (Exception e){
                                alt_val = 0.0f;
                                continue;
                            }
                            this.alt = alt_val;
                            System.out.println("Altitude:");
                            System.out.println(alt);
                            //End of Data collection
                            return 0;
                    }
                    count++;
                }
            }
        }

    }
}
