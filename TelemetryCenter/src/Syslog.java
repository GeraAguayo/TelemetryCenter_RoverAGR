import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class Syslog {

    Map<Integer,String>  syslog_dict = new Hashtable<>();

    //Constructor
    public Syslog(){
        //Initialize values
        syslog_dict.put(0,"The Rover has been disconnected from the network");
        syslog_dict.put(1,"The Rover is connected");
        syslog_dict.put(2,"BMP 280 sensor failed to initialize");
        syslog_dict.put(3,"Connection failed with telemetry Arduino");
    }

    public String getMessage(int key){
        return syslog_dict.get(key);
    }
}
