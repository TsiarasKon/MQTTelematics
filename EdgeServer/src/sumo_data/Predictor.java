package sumo_data;

import static java.lang.Math.*;

public class Predictor {
    private static final double earthR = 6.371E6;    // meters

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

    private void predictLatLon(double lat, double lon, double angle, double speed) {
        double latRads = toRadians(lat);
        double lonRads = toRadians(lon);
        double angleRads = toRadians(angle);
        double delta = (1 * speed) / earthR;
        predictedLat = toDegrees( asin(sin(latRads) * cos(delta) + cos(latRads) * sin(delta) * cos(angleRads)) );
        predictedLon = toDegrees( lonRads + atan2(sin(angleRads) * sin(delta) * cos(latRads), cos(delta) - sin(latRads) * sin(predictedLat)) );
//        System.out.println("New: " + predictedLat + ',' + predictedLon);
//        System.out.println("dist: " + distance(lat, lon, predictedLat, predictedLon, "K"));
    }


    public static double distance(double lat1, double lon1, double lat2, double lon2, String unit) {
        /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/
        /*::                                                                         :*/
        /*::  This routine calculates the distance between two points (given the     :*/
        /*::  latitude/longitude of those points). It is being used to calculate     :*/
        /*::  the distance between two locations using GeoDataSource (TM) products   :*/
        /*::                                                                         :*/
        /*::  Definitions:                                                           :*/
        /*::    Southern latitudes are negative, eastern longitudes are positive     :*/
        /*::                                                                         :*/
        /*::  Function parameters:                                                   :*/
        /*::    lat1, lon1 = Latitude and Longitude of point 1 (in decimal degrees)  :*/
        /*::    lat2, lon2 = Latitude and Longitude of point 2 (in decimal degrees)  :*/
        /*::    unit = the unit you desire for results                               :*/
        /*::           where: 'M' is statute miles (default)                         :*/
        /*::                  'K' is kilometers                                      :*/
        /*::                  'N' is nautical miles                                  :*/
        /*::  Worldwide cities and other features databases with latitude longitude  :*/
        /*::  are available at https://www.geodatasource.com                         :*/
        /*::                                                                         :*/
        /*::  For enquiries, please contact sales@geodatasource.com                  :*/
        /*::                                                                         :*/
        /*::  Official Web site: https://www.geodatasource.com                       :*/
        /*::                                                                         :*/
        /*::           GeoDataSource.com (C) All Rights Reserved 2019                :*/
        /*::                                                                         :*/
        /*::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::::*/

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        }
        else {
            double theta = lon1 - lon2;
            double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2)) + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            dist = Math.acos(dist);
            dist = Math.toDegrees(dist);
            dist = dist * 60 * 1.1515;
            if (unit.equals("K")) {
                dist = dist * 1.609344;
            } else if (unit.equals("N")) {
                dist = dist * 0.8684;
            }
            return (dist);
        }
    }
}
