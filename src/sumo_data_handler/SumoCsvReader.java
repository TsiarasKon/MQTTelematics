package sumo_data_handler;

// TODO: Limits arrays may be unnecessary
// TODO: try formula "(x - min) / (max - min)" or similar then mod

public class SumoCsvReader {
    private double min_lat;
    private double max_lat;
    private double min_lon;
    private double max_lon;
    private int heightCells;
    private int widthCells;
    private double[] latLimits;
    private double[] lonLimits;
    private int[][] rssiCellMap;
    private double[][] throughputCellMap;

    public SumoCsvReader(double min_lat, double max_lat, double min_lon, double max_lon, int heightCells, int widthCells) {
        this.min_lat = min_lat;
        this.max_lat = max_lat;
        this.min_lon = min_lon;
        this.max_lon = max_lon;
        this.heightCells = heightCells;
        this.widthCells = widthCells;

        latLimits = new double[heightCells - 1];
        double latIncrement = (max_lat - min_lat) / heightCells;
        latLimits[0] = min_lat + latIncrement;
        for (int i = 1; i < heightCells - 1; i++) {
            latLimits[i] = latLimits[i - 1] + latIncrement;
        }
        lonLimits = new double[widthCells - 1];
        double lonIncrement = (max_lon - min_lon) / widthCells;
        lonLimits[0] = min_lon + lonIncrement;
        for (int i = 1; i < widthCells - 1; i++) {
            lonLimits[i] = lonLimits[i - 1] + lonIncrement;
        }

        rssiCellMap = new int[heightCells][];
        throughputCellMap = new double[heightCells][];
        for (int i = 0; i < heightCells; i++) {
            rssiCellMap[i] = new int[widthCells];
            throughputCellMap[i] = new double[widthCells];
        }
    }

//    private <T> void printCellMap(T[][] cellmap) {
//        for (int i = 0; i < heightCells; i++) {
//            for (int j = 0; j < widthCells; j++) {
//                System.out.println(cellmap[i][j] + " ");
//            }
//            System.out.println();
//        }
//    }

    public void readCsv() {
        // TODO: save data from csv to arrays
//        for (int i = 0; i < heightCells; i++) {
//            for (int j = 0; j < widthCells; j++) {
//                System.out.print(rssiCellMap[i][j] + " ");
//            }
//            System.out.println();
//        }
//        System.out.println();
//        for (int i = 0; i < heightCells; i++) {
//            for (int j = 0; j < widthCells; j++) {
//                System.out.print(throughputCellMap[i][j] + " ");
//            }
//            System.out.println();
//        }
    }

}
