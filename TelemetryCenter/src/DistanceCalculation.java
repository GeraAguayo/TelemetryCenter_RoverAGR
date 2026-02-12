import java.util.ArrayDeque;
import java.util.Queue;

public class DistanceCalculation {
    public double distance_traveled = 0;
    private Queue<Float> latHistory = new ArrayDeque<>();
    private Queue<Float> lonHistory = new ArrayDeque<>();
    private static final int SIZE = 15;
    private double last_avg_lat = 0.0;
    private double last_avg_lon = 0.0;


    void addCoordinates(float lat, float lon){
        latHistory.add(lat);
        lonHistory.add(lon);
        if (latHistory.size() > SIZE){
            latHistory.poll();
            lonHistory.poll();
        }
    }

    private double[] getAverage(){
        double sum_lat = 0;
        double sum_lon = 0;
        for (float l : latHistory){
            sum_lat+=l;
        }
        for (float l : lonHistory){
            sum_lon+=l;
        }
        return new double[]{sum_lat / latHistory.size(), sum_lon / lonHistory.size()};
    }

    public double calculateDelta(){
        //return delta distance between 2 coordinates
        if (latHistory.size() < SIZE) return 0;
        double[] currentAvg = getAverage();
        //if first time, initialize last point with current average
        if (last_avg_lat == 0.0){
            last_avg_lat = currentAvg[0];
            last_avg_lon = currentAvg[1];
            return 0;
        }


        double R = 6371000; //earth radius
        //degrees to rad
        double dLat = Math.toRadians(currentAvg[0] - last_avg_lat);
        double dLon = Math.toRadians(currentAvg[1] - last_avg_lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(last_avg_lat)) * Math.cos(Math.toRadians(currentAvg[0])) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double delta =  R * (2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a)));

        last_avg_lat = currentAvg[0];
        last_avg_lon = currentAvg[1];
        if (delta >= 0.5){
            last_avg_lat = currentAvg[0];
            last_avg_lon = currentAvg[1];
            distance_traveled+=delta;
            return delta;
        }
        return 0;
    }
}
