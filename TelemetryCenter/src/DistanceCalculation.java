public class DistanceCalculation {
    float total_distance = 0;

    public double calculateDelta(float lat_1, float lon_1, float lat_2, float lon_2){
        //return delta distance between 2 coordinates
        double R = 6371000; //earth radius

        //degrees to rad
        double dLat = Math.toRadians(lat_2 - lat_1);
        double dLon = Math.toRadians(lon_2 - lon_1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat_1)) * Math.cos(Math.toRadians(lat_2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double result = R * c;
        total_distance+=result;

        return result;
    }
}
