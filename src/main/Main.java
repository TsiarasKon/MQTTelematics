package main;

import sumo_data_handler.Heatmap;
import sumo_data_handler.SumoXml2csv;

import java.awt.*;

public class Main {
    private static String ouputDirPath = "out/";

    public static void main(String[] args) {

        SumoXml2csv sumo = new SumoXml2csv();
        sumo.xml2csvConvert("res/all_vehicles.xml", ouputDirPath);

        Heatmap heatmap = new Heatmap(4, 10, Color.red, Color.green);
        heatmap.generateHeatmap("res/Map.jpg", ouputDirPath);
    }
}
