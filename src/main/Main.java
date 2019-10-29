package main;

import sumo_data_handler.Heatmap;
import sumo_data_handler.SumoCsvReader;
import sumo_data_handler.SumoXml2Csv;

import java.awt.*;

public class Main {
    private static final String ouputDirPath;
    private static final double min_lat;
    private static final double max_lat;
    private static final double min_lon;
    private static final double max_lon;
    private static final int heatmapHeightCells;
    private static final int heatmapWidthCells;

    static {
        ouputDirPath = "out/";
        min_lat = 37.9668800;
        max_lat = 37.9686200;
        min_lon = 23.7647600;
        max_lon = 23.7753900;
        heatmapHeightCells = 4;
        heatmapWidthCells = 10;
    }

    public static void main(String[] args) {

        SumoXml2Csv sumoConverter = new SumoXml2Csv(min_lat, max_lat, min_lon, max_lon);
        sumoConverter.xml2csvConvert("res/all_vehicles.xml", ouputDirPath);

        SumoCsvReader sumoReader = new SumoCsvReader(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells);
//        sumoReader.readCsv();

        Heatmap heatmap = new Heatmap(heatmapHeightCells, heatmapWidthCells, Color.red, Color.green);
        heatmap.generateHeatmap("res/Map.jpg", ouputDirPath);
    }
}
