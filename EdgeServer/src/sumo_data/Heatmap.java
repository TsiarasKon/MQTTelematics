package sumo_data;

import org.tc33.jheatchart.HeatChart;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Locale;

public class Heatmap {
    private int heightCells;
    private int widthCells;
    private double[][] cellMap;
    private double[][] normalizedCellMap;

    private Color lowColor = Color.red;
    private Color highColor = Color.green;
    private float overlayOpacity = 0.5f;
    private boolean normalizeFlag = true;
    private boolean writeValuesFlag = true;

    public Heatmap(int heightCells, int widthCells, double[][] cellMap) {
        this.heightCells = heightCells;
        this.widthCells = widthCells;
        this.cellMap = cellMap;
        this.normalizedCellMap = new double[heightCells][];
        for (int i = 0; i < heightCells; i++)
            this.normalizedCellMap[i] = new double[widthCells];
    }

    public void generateHeatmap(String mapFilepath, String outputFilepath, String legendFilepath) {

        BufferedImage baseMap;
        try {
            baseMap = ImageIO.read(new File(mapFilepath));
        } catch (IOException e) {
            System.err.println("Failed to open '" + mapFilepath + "'");
            return;
        }

        HeatChart map;
        if (normalizeFlag) {
            normalizeEmptyCells();
            map = new HeatChart(normalizedCellMap);
        } else {
            map = new HeatChart(cellMap);
        }
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

        // Optional: save plain heatmap to a file
//        try {
//            map.saveToFile(new File(outputDirpath + "heatmap.png"));
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        Graphics2D g2 = baseMap.createGraphics();
        g2.setComposite(AlphaComposite.SrcOver.derive(overlayOpacity));
        g2.drawImage(map.getChartImage(), 0, 0, null);
        if (writeValuesFlag) {
            writeCellValues(g2, baseMap.getWidth(), baseMap.getHeight());
        }
        g2.dispose();

        // concat heatmap legend
        BufferedImage heatmapLegend;
        BufferedImage resultMap;
        try {
            heatmapLegend = ImageIO.read(new File(legendFilepath));
            resultMap = new BufferedImage(baseMap.getWidth() + heatmapLegend.getWidth(), baseMap.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics g2f = resultMap.getGraphics();
            g2f.drawImage(baseMap, 0, 0, null);
            g2f.drawImage(heatmapLegend, baseMap.getWidth(), 0, null);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Failed to open '" + legendFilepath + "'; printing map without a legend");
            resultMap = baseMap;
        }

        File outputfile = new File(outputFilepath);
        try {
            ImageIO.write(resultMap, "png", outputfile);
        } catch (IOException e) {
            System.err.println("Failed to create '" + outputFilepath + "'");
            e.printStackTrace();
        }
    }

    private double getCellMapAvg() {
        double cellMapAvg = 0;
        int nonEmptyCellNum = 0;
        for (int i = 0; i < cellMap.length; i++)
            for (int j = 0; j < cellMap[i].length; j++)
                if (cellMap[i][j] != 0) {
                    cellMapAvg += cellMap[i][j];
                    nonEmptyCellNum++;
                }
        return cellMapAvg / nonEmptyCellNum;
    }

    public double getPointVal(int latIndex, int lonIndex) {
        return cellMap[latIndex][lonIndex];
    }

    private void normalizeEmptyCells() {
        double cellMapAvg = getCellMapAvg();
        for (int i = 0; i < cellMap.length; i++)
            for (int j = 0; j < cellMap[i].length; j++)
                if (cellMap[i][j] == 0)
                    normalizedCellMap[i][j] = cellMapAvg;
                else
                    normalizedCellMap[i][j] = cellMap[i][j];
    }

    private void writeCellValues(Graphics2D g2, int mapWidth, int mapHeight) {
        int widthStart = (int) ((mapWidth / widthCells) * 0.28);
        int heightStart = (int) ((mapHeight / heightCells) * 0.7);
        g2.setPaint(Color.black);
        g2.setFont(g2.getFont().deriveFont(30f));
        for (int i = 0, h = heightStart; i < cellMap.length; i++, h += mapHeight / heightCells)
            for (int j = 0, w = widthStart; j < cellMap[i].length; j++, w += mapWidth / widthCells)
                if (cellMap[i][j] == 0)
                    g2.drawString("0", w + (int) ((mapWidth / widthCells) * 0.24), h);
                else
                    g2.drawString(String.format(Locale.US, "%.2f", cellMap[i][j]), w, h);
    }
}
