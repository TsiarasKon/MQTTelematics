package main;

import sumo_data_handler.Heatmap;
import sumo_data_handler.SumoCsvReader;
import sumo_data_handler.SumoXml2Csv;

public class Main {
    private static final String[] vehiclesXmlPaths;
    private static final String[] outputVehiclesCsvPaths;
    private static final String baseMapPath;
    private static final String[] outputHeatmapPaths;
    private static final double min_lat;
    private static final double max_lat;
    private static final double min_lon;
    private static final double max_lon;
    private static final int heatmapHeightCells;
    private static final int heatmapWidthCells;

    static {
        vehiclesXmlPaths = new String[]{
                "res/all_vehicles.xml",
                "res/vehicle_26.xml",
                "res/vehicle_27.xml"
        };
        outputVehiclesCsvPaths = new String[]{
                "out/all_vehicles.csv",
                "out/vehicle_26.csv",
                "out/vehicle_27.csv"
        };
        baseMapPath = "res/Map.jpg";
        outputHeatmapPaths = new String[]{
                "out/Heatmap_RSSI.png",
                "out/Heatmap_Throughput.png"
        };
        min_lat = 37.9668800;
        max_lat = 37.9686200;
        min_lon = 23.7647600;
        max_lon = 23.7753900;
        heatmapHeightCells = 4;
        heatmapWidthCells = 10;
    }

    public static void main(String[] args) {
        System.out.println("Converting XML files to CSV ...");
        SumoXml2Csv sumoConverter = new SumoXml2Csv(min_lat, max_lat, min_lon, max_lon);
        sumoConverter.xml2csvConvert(vehiclesXmlPaths[0], outputVehiclesCsvPaths[0]);
        sumoConverter.xml2csvConvert(vehiclesXmlPaths[1], outputVehiclesCsvPaths[1]);
        sumoConverter.xml2csvConvert(vehiclesXmlPaths[2], outputVehiclesCsvPaths[2]);

        System.out.println("Loading data from '" + outputVehiclesCsvPaths[0] + "' ...");
        SumoCsvReader sumoReader = new SumoCsvReader(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells);
        sumoReader.readCsv(outputVehiclesCsvPaths[0]);

        // TODO: heatmap values need further handling (many 0s, small variation in the rest)
        System.out.println("Creating RSSI heatmap at '" + outputHeatmapPaths[0] + "' ...");
        Heatmap heatmapRSSI = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getRssiCellMap());
        heatmapRSSI.generateHeatmap(baseMapPath, outputHeatmapPaths[0]);

        System.out.println("Creating Throughput heatmap at '" + outputHeatmapPaths[1] + "' ...");
        Heatmap heatmapThroughput = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getThroughputCellMap());
        heatmapThroughput.generateHeatmap(baseMapPath, outputHeatmapPaths[1]);
    }
}
