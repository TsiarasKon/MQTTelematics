package database;

import predictions.Predictor;

public class RealPredictedLatLon {
    public double real_lat;
    public double real_long;
    public double predicted_lat;
    public double predicted_long;

    public RealPredictedLatLon(double real_lat, double real_long, double predicted_lat, double predicted_long) {
        this.real_lat = real_lat;
        this.real_long = real_long;
        this.predicted_lat = predicted_lat;
        this.predicted_long = predicted_long;
    }

    public double getPredictionError() {        // in meters
        return Predictor.distance(real_lat, real_long, predicted_lat, predicted_long, "K") * 1000;
    }
}
