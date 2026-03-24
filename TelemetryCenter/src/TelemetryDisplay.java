import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvException;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;

import javax.swing.*;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TelemetryDisplay extends JFrame {

    private JPanel MainPanel;
    private JLabel Title;
    private JPanel subPanel1;
    private JPanel subPanel2;
    private JPanel subPanel3;
    private JPanel subPanel4;
    private JPanel subPanel5;
    private JPanel subPanel6;
    private JPanel subPanel7;
    private JPanel subPanel8;
    private JButton loadBtn;

    ArrayList<String> timestamp_collection = new ArrayList<>();
    ArrayList<Float> temperature_colletion = new ArrayList<>();
    ArrayList<Float> altitude_collection = new ArrayList<>();
    ArrayList<Float> pressure_collection = new ArrayList<>();
    ArrayList<Float> humidity_collection = new ArrayList<>();
    ArrayList<Float> gas_collection = new ArrayList<>();
    ArrayList<Float> latitude_collection = new ArrayList<>();
    ArrayList<Float> longitude_collection = new ArrayList<>();
    ArrayList<Float> distance_traveled_collection = new ArrayList<>();

    public TelemetryDisplay(){
        this.setTitle("Telemetry AGR");
        try {
            ImageIcon img = new ImageIcon(getClass().getResource("/media/logo_white_200.png"));
            this.setIconImage(img.getImage());
        } catch (Exception e) {
            System.out.println("Icon not found: " + e.getMessage());
        }
        this.setContentPane(MainPanel);
        this.pack();
        this.setSize(1920,1080);

        //listener for load csv btn
        loadBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileDialog fd = new FileDialog((Frame)null, "Select a file", FileDialog.LOAD);
                fd.setFile("*.csv");
                fd.setVisible(true);

                String filename = fd.getFile();
                if (filename != null){
                    String fullPath = fd.getDirectory() + filename;
                    try {
                        readData(fullPath);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, ex.getMessage(), "File Error", JOptionPane.ERROR_MESSAGE);
                    }
                    renderGraphs();
                }
                else{
                    JOptionPane.showMessageDialog(null, "No valid CSV selected", "File Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }

    private void clearCollections(){
        timestamp_collection.clear();
        temperature_colletion.clear();
        altitude_collection.clear();
        pressure_collection.clear();
        gas_collection.clear();
        latitude_collection.clear();
        longitude_collection.clear();
        distance_traveled_collection.clear();

    }

    private void readData (String filepath) throws IOException, CsvException {
        FileReader filereader = new FileReader(filepath);
        CSVReader csvReader = new CSVReaderBuilder(filereader)
                .withSkipLines(1)
                .build();

        List<String[]> allData = csvReader.readAll();
        clearCollections();

        for (String[] row : allData){
            timestamp_collection.add(row[1]);
            temperature_colletion.add(Float.valueOf(row[2]));
            altitude_collection.add(Float.valueOf(row[3]));
            pressure_collection.add(Float.valueOf(row[4]));
            humidity_collection.add(Float.valueOf(row[5]));
            gas_collection.add(Float.valueOf(row[6]));
            latitude_collection.add(Float.valueOf(row[7]));
            longitude_collection.add(Float.valueOf(row[8]));
            distance_traveled_collection.add(Float.valueOf(row[9]));
        }
    }

    private void addGraphToPanel(JPanel targetPanel, String title, String yLabel, String rowKey, ArrayList<Float> data) {

        LineChart manager = new LineChart();
        JFreeChart chart = manager.createChartFloat(
                title, "Time", yLabel, rowKey, data, timestamp_collection
        );

        ChartPanel chartPanel = new ChartPanel(chart);
        targetPanel.removeAll();
        targetPanel.setLayout(new java.awt.BorderLayout());
        targetPanel.add(chartPanel, java.awt.BorderLayout.CENTER);
        targetPanel.revalidate();
        targetPanel.repaint();
    }
    private void renderGraphs() {
        addGraphToPanel(subPanel1, "Temperature", "C°", "Temp", temperature_colletion);
        addGraphToPanel(subPanel2, "Altitude", "m", "Alt", altitude_collection);
        addGraphToPanel(subPanel3, "Pressure", "hPa", "Press", pressure_collection);
        addGraphToPanel(subPanel4, "Humidity", "%", "Hum", humidity_collection);
        addGraphToPanel(subPanel5, "Gas Resistance", "Ω", "Gas", gas_collection);
        addGraphToPanel(subPanel6, "Latitude", "deg", "Lat", latitude_collection);
        addGraphToPanel(subPanel7, "Longitude", "deg", "Lon", longitude_collection);
        addGraphToPanel(subPanel8, "Distance", "m", "Dist", distance_traveled_collection);
    }

}
