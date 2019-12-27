import network.ESSubscriber;
import network.EdgeServer;
import sumo_data.Heatmap;
import sumo_data.Predictor;
import sumo_data.SumoCsvReader;
import sumo_data.SumoXml2Csv;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.eclipse.paho.client.mqttv3.MqttException;

public class Main {
    private static final String[] vehiclesXmlPaths;
    private static final String[] outputVehiclesCsvPaths;
    private static final String baseMapPath;
    private static final String heatmapLegendPath;
    private static final String[] outputHeatmapPaths;
    private static final double min_lat;
    private static final double max_lat;
    private static final double min_lon;
    private static final double max_lon;
    private static final int heatmapHeightCells;
    private static final int heatmapWidthCells;

    static {
        vehiclesXmlPaths = new String[]{
                "res/xml/all_vehicles.xml",
                "res/xml/vehicle_26.xml",
                "res/xml/vehicle_27.xml"
        };
        outputVehiclesCsvPaths = new String[]{
                "out/all_vehicles.csv",
                "out/vehicle_26.csv",
                "out/vehicle_27.csv"
        };
        baseMapPath = "res/images/Map.jpg";
        heatmapLegendPath = "res/images/heatmap_legend.png";
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
        List<String> argsList = Arrays.asList(args);

        if (argsList.isEmpty() || argsList.contains("-h")) {
            System.out.println("Available options:");
            System.out.println(" -c : Convert XML files to CSV");
            System.out.println(" -g : Generate heatmaps from CSV files");
            System.out.println(" -s : Subscribe to vehicle MQTT topics - Mosquitto must already be running");
            System.out.println(" -h : This help menu");
        }

        if (argsList.contains("-c")) {
            File directory = new File("out/");
            if (! directory.exists()){
                directory.mkdir();
            }
            System.out.println("Converting XML files to CSV ...");
            SumoXml2Csv sumoConverter = new SumoXml2Csv(min_lat, max_lat, min_lon, max_lon);
            sumoConverter.xml2csvConvert(vehiclesXmlPaths[0], outputVehiclesCsvPaths[0]);
            sumoConverter.xml2csvConvert(vehiclesXmlPaths[1], outputVehiclesCsvPaths[1]);
            sumoConverter.xml2csvConvert(vehiclesXmlPaths[2], outputVehiclesCsvPaths[2]);
        }

        Heatmap heatmapRSSI = null;
        Heatmap heatmapThroughput = null;

        if (argsList.contains("-g")) {
            System.out.println("Loading data from '" + outputVehiclesCsvPaths[0] + "' ...");
            SumoCsvReader sumoReader = new SumoCsvReader(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells);
            sumoReader.readCsv(outputVehiclesCsvPaths[0]);

            System.out.println("Creating RSSI heatmap at '" + outputHeatmapPaths[0] + "' ...");
            heatmapRSSI = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getRssiCellMap());
            heatmapRSSI.generateHeatmap(baseMapPath, outputHeatmapPaths[0], heatmapLegendPath);

            System.out.println("Creating Throughput heatmap at '" + outputHeatmapPaths[1] + "' ...");
            heatmapThroughput = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getThroughputCellMap());
            heatmapThroughput.generateHeatmap(baseMapPath, outputHeatmapPaths[1],heatmapLegendPath);
        }

        if (argsList.contains("-s")) {
            if (!argsList.contains("-g")) {     // heatmaps were not previously created but we still need them
                SumoCsvReader sumoReader = new SumoCsvReader(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells);
                heatmapRSSI = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getRssiCellMap());
                heatmapThroughput = new Heatmap(heatmapHeightCells, heatmapWidthCells, sumoReader.getThroughputCellMap());
            }

            Predictor v26Predictor = new Predictor(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells, heatmapRSSI, heatmapThroughput);
            Predictor v27Predictor = new Predictor(min_lat, max_lat, min_lon, max_lon, heatmapHeightCells, heatmapWidthCells, heatmapRSSI, heatmapThroughput);

            try {
                System.out.println("Listening to '" + EdgeServer.getV2ESTopic(0) + "' and '" + EdgeServer.getV2ESTopic(1) + "'");
                ESSubscriber vs26sub = new ESSubscriber("ES_V26", EdgeServer.getV2ESTopic(0), EdgeServer.getES2VTopic(0), v26Predictor);
                ESSubscriber vs27sub = new ESSubscriber("ES_V27", EdgeServer.getV2ESTopic(1), EdgeServer.getES2VTopic(1), v27Predictor);
                System.out.println("Press Enter to terminate the Edge Server ...");
                new Scanner(System.in).nextLine();
                vs26sub.disconnect();
                vs27sub.disconnect();
                System.out.println("Disconnected from '" + EdgeServer.getV2ESTopic(0) + "' and '" + EdgeServer.getV2ESTopic(1) + "'");
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
