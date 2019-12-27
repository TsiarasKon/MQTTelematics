package database;

public class DBDatapoint {
    public double timestep;
    public int device_id;
    public double real_lat;
    public double real_long;
    public double predicted_lat;
    public double predicted_long;
    public double real_RSSI;
    public double real_throughput;
    public double predicted_RSSI;
    public double predicted_throughput;

    public DBDatapoint(double real_lat, double real_long, double predicted_lat, double predicted_long) {
        this.real_lat = real_lat;
        this.real_long = real_long;
        this.predicted_lat = predicted_lat;
        this.predicted_long = predicted_long;
    }

    public DBDatapoint(double timestep, int device_id, double real_lat, double real_long, double predicted_lat, double predicted_long, double real_RSSI, double real_throughput, double predicted_RSSI, double predicted_throughput) {
        this.timestep = timestep;
        this.device_id = device_id;
        this.real_lat = real_lat;
        this.real_long = real_long;
        this.predicted_lat = predicted_lat;
        this.predicted_long = predicted_long;
        this.real_RSSI = real_RSSI;
        this.real_throughput = real_throughput;
        this.predicted_RSSI = predicted_RSSI;
        this.predicted_throughput = predicted_throughput;
    }
}
