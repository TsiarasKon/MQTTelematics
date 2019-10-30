package sumo_data_handler;

import org.tc33.jheatchart.HeatChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class Heatmap {
    private int heightCells;
    private int widthCells;
    private double[][] cellMap;

    private Color lowColor = Color.red;
    private Color highColor = Color.green;
    private float overlayOpacity = 0.5f;

    public Heatmap(int heightCells, int widthCells, double[][] cellMap) {
        this.heightCells = heightCells;
        this.widthCells = widthCells;
        this.cellMap = cellMap;
    }

    public void generateHeatmap(String mapFilepath, String outputFilepath) {

        BufferedImage baseMap;
        try {
            baseMap = ImageIO.read(new File(mapFilepath));
        } catch (IOException e) {
            System.err.println("Failed to open '" + mapFilepath + "'");
            return;
        }

//        // dummy data here:
//        double[][] data = new double[][]{{3,2,7,4,5,6,4,5,3,7},
//                {2,3,4,5,6,7,7,3,6,5},
//                {3,4,5,6,7,6,5,4,3,3},
//                {4,5,6,4,6,5,3,7,7,4}};

        HeatChart map = new HeatChart(cellMap);
        map.setLowValueColour(lowColor);
        map.setHighValueColour(highColor);
        map.setCellSize(new Dimension(baseMap.getWidth() / widthCells, baseMap.getHeight() / heightCells));
        map.setTitle(null);
        map.setXAxisLabel(null);
        map.setYAxisLabel(null);
        map.setShowXAxisValues(false);
        map.setShowYAxisValues(false);
        map.setChartMargin(2);
        map.setAxisThickness(4);

//        // Optional: save plain heatmap to a file
//        try {
//            map.saveToFile(new File(outputDirpath + "heatmap.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Graphics2D g2 = baseMap.createGraphics();
        g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
        g2.drawImage(map.getChartImage(), 0, 0, null);
        g2.dispose();
        File outputfile = new File(outputFilepath);
        try {
            ImageIO.write(baseMap, "png", outputfile);
        } catch (IOException e) {
            System.err.println("Failed to create '" + outputFilepath + "'");
            e.printStackTrace();
        }
    }
}
