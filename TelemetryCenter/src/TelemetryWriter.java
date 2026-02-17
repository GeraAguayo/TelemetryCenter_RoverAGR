import com.opencsv.CSVWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


public class TelemetryWriter {

    private String FILE_NAME = "";
    private static final String[] HEADER = {
            "Date","Time","Temperature","Altitude", "Pressure",
            "Humidity","Gas","Latitude","Longitude","Distance Traveled"
    };
    private boolean file_started = false;


    public void startFile(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter date_f = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        DateTimeFormatter time_f = DateTimeFormatter.ofPattern("HH-mm");
        String date_string = myDateObj.format(date_f);
        String time_string = myDateObj.format(time_f);
        FILE_NAME = "telemetry_agr_" + date_string + "-" + time_string + ".csv";
        file_started = true;

    }

    public void endFile(){
        FILE_NAME = "";
        file_started = false;
    }

    public void logTelemetry(String[] telemetryRow) throws IOException {
        if (!file_started) return;
        String current_path = System.getProperty("user.dir");
        File file = new File(Paths.get(current_path, FILE_NAME).toString());

        boolean is_new_file = !file.exists();

        try (CSVWriter writer = new CSVWriter(new FileWriter(file, true))){
            if (is_new_file){
                writer.writeNext(HEADER);
            }
            writer.writeNext(telemetryRow);
        } catch (IOException e){
            System.err.println("Failure at writing CSV: " + e.getMessage());
        }
    }



}
