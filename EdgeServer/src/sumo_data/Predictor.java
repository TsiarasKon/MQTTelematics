package sumo_data;

public class Predictor {
    private double min_lat;
    private double max_lat;
    private double min_lon;
    private double max_lon;
    private int heightCells;
    private int widthCells;
    private double[][] rssiCellMap;
    private double[][] throughputCellMap;

    private double predictedLat;
    private double predictedLon;

    public Predictor(double min_lat, double max_lat, double min_lon, double max_lon, int heightCells, int widthCells, double[][] rssiCellMap, double[][] throughputCellMap) {
        this.min_lat = min_lat;
        this.max_lat = max_lat;
        this.min_lon = min_lon;
        this.max_lon = max_lon;
        this.heightCells = heightCells;
        this.widthCells = widthCells;
        this.rssiCellMap = rssiCellMap;
        this.throughputCellMap = throughputCellMap;
    }

    public String makePredictionFor(double lat, double lon, double angle, double speed) {
        predictLatLon(lat, lon, angle, speed);
        // latitudes are reversed!
        int latIndex = heightCells - 1 - SumoCsvReader.getCellMapIndex(predictedLat, min_lat, max_lat, heightCells);
        int lonIndex = SumoCsvReader.getCellMapIndex(predictedLon, min_lon, max_lon, widthCells);
        return predictedLat + "," + predictedLon + "," + rssiCellMap[latIndex][lonIndex] + ","
                + throughputCellMap[latIndex][lonIndex];
    }

    public void predictLatLon(double lat, double lon, double angle, double speed) {
        // TODO
        predictedLat = lat;
        predictedLon = lon;
    }
}
