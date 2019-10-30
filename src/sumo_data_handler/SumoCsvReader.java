package sumo_data_handler;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SumoCsvReader {
    private double min_lat;
    private double max_lat;
    private double min_lon;
    private double max_lon;
    private int heightCells;
    private int widthCells;
    private double[][] rssiCellMap;
    private double[][] throughputCellMap;
    private List<Integer>[][] rssiCellMapLists;
    private List<Double>[][] throughputCellMapLists;

    public SumoCsvReader(double min_lat, double max_lat, double min_lon, double max_lon, int heightCells, int widthCells) {
        this.min_lat = min_lat;
        this.max_lat = max_lat;
        this.min_lon = min_lon;
        this.max_lon = max_lon;
        this.heightCells = heightCells;
        this.widthCells = widthCells;

        rssiCellMap = new double[heightCells][];
        throughputCellMap = new double[heightCells][];
        rssiCellMapLists = (List<Integer>[][]) new ArrayList[heightCells][widthCells];
        throughputCellMapLists = (List<Double>[][]) new ArrayList[heightCells][widthCells];
        for (int i = 0; i < heightCells; i++) {
            rssiCellMap[i] = new double[widthCells];
            throughputCellMap[i] = new double[widthCells];
            for (int j = 0; j < widthCells; j++) {
                rssiCellMapLists[i][j] = new ArrayList<Integer>();
                throughputCellMapLists[i][j] = new ArrayList<Double>();
            }
        }
    }

    public double[][] getRssiCellMap() {
        return rssiCellMap;
    }

    public double[][] getThroughputCellMap() {
        return throughputCellMap;
    }

    private int getCellMapIndex(double val, double minL, double maxL, int cellsNum) {
        if (val < minL || val > maxL) {     // should never get here:
            System.err.println("Unexpected value out of lat/lon limits found in cellMap");
        }
        int index = (int) ((val - minL) / (maxL - minL) * cellsNum);
        return (index == cellsNum) ? (cellsNum - 1) : index;
    }

//    // generics won't work :(
//    private <T> void printCellMap(T[][] cellmap) {
//        for (int i = 0; i < heightCells; i++) {
//            for (int j = 0; j < widthCells; j++) {
//                System.out.println(cellmap[i][j] + " ");
//            }
//            System.out.println();
//        }
//    }
//
//    private <T extends Number> double getListAverage(List<T> list) {
//        if (list.size() == 0) return 0.0;
//        double avg = 0.0;
//        for (T num : list) {
//            avg += num;
//        }
//        return avg / list.size();
//    }

    private double getIntListAverage(List<Integer> list) {
        if (list.size() == 0) return 0.0;
        double avg = 0.0;
        for (int num : list) {
            avg += num;
        }
        return avg / list.size();
    }

    private double getDoubleListAverage(List<Double> list) {
        if (list.size() == 0) return 0.0;
        double avg = 0.0;
        for (double num : list) {
            avg += num;
        }
        return avg / list.size();
    }

    public void readCsv(String csvFilepath) {
        String line;
        BufferedReader br = null;
        String[] vehicleDataRow;
        int latIndex, lonIndex;
        try {
            br = new BufferedReader(new FileReader(csvFilepath));
            // populate cellMap lists:
            while ((line = br.readLine()) != null) {
                vehicleDataRow = line.split(",");
                latIndex = getCellMapIndex(Double.parseDouble(vehicleDataRow[2]), min_lat, max_lat, heightCells);
                lonIndex = getCellMapIndex(Double.parseDouble(vehicleDataRow[3]), min_lon, max_lon, widthCells);
                rssiCellMapLists[latIndex][lonIndex].add(Integer.parseInt(vehicleDataRow[6]));
                throughputCellMapLists[latIndex][lonIndex].add(Double.parseDouble(vehicleDataRow[7]));
            }
            // calculate average RSSI and throughput for each cell:
            for (int i = 0; i < heightCells; i++) {
                for (int j = 0; j < widthCells; j++) {
                    rssiCellMap[i][j] = getIntListAverage(rssiCellMapLists[i][j]);
                    throughputCellMap[i][j] = getDoubleListAverage(throughputCellMapLists[i][j]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        // For debugging:
        for (int i = 0; i < heightCells; i++) {
            for (int j = 0; j < widthCells; j++) {
                System.out.print(String.format(Locale.US, "%5.2f", rssiCellMap[i][j]) + " ");
            }
            System.out.println();
        }
        System.out.println();
        for (int i = 0; i < heightCells; i++) {
            for (int j = 0; j < widthCells; j++) {
                System.out.print(String.format(Locale.US, "%5.2f", throughputCellMap[i][j]) + " ");
            }
            System.out.println();
        }
    }

}
