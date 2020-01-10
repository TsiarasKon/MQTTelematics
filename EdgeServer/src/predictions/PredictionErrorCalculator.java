package predictions;

import database.DBBridge;
import database.RealPredictedLatLon;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PredictionErrorCalculator {
    private int terminalId;

    public PredictionErrorCalculator(int terminalId) {
        this.terminalId = terminalId;
    }

    public void calculateError(String outputChartPath) {
        DBBridge db;
        try {
            db = new DBBridge();
        } catch (ClassNotFoundException | SQLException e) {
            System.err.println("Database connection failed - DB may be offline");
            return;
        }
        List<RealPredictedLatLon> list = db.getTerminalRealPredictedLatLons(terminalId);
        db.close();
        List<Double> errorList = new ArrayList<>();
        for (RealPredictedLatLon datapoint : list) {
            // skip first datapoint (no predicted value) and last (no real value) [sorry Null Island!]
            if (datapoint.real_lat == 0.0 || datapoint.predicted_lat == 0.0) continue;
            errorList.add(datapoint.getPredictionError());
        }
        if (errorList.size() > 0) {
            double errorSum = 0.0;
            for (double err : errorList) {
                errorSum += err;
            }
            if (outputChartPath != null) {
                visualizeErrors(errorList, outputChartPath);
                System.out.println("[Vehicle " + terminalId + "] Error visualization saved in '" + outputChartPath + "'");
            }
            System.out.println("[Vehicle " + terminalId + "] Average error: " + String.format(Locale.US, "%.3f", errorSum / errorList.size()) + " meters");
        } else {
            System.out.println("[Vehicle " + terminalId + "] No predictions were made for this vehicle!");
        }
    }

    private void visualizeErrors(List<Double> errorList, String outputChartPath) {
        DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
        double timestep = 1.0;
        double avg = 0.0;
        for (double val : errorList) {
            chartDataset.addValue(val, "Prediction Error", String.valueOf(timestep));
            avg *= (timestep - 1) / timestep;
            avg += val * (1/ timestep);
            chartDataset.addValue(avg, "Average Prediction Error", String.valueOf(timestep));
            timestep++;
        }

        JFreeChart lineChartObject = ChartFactory.createLineChart(
                "Prediction Error per timestep","Timestep (in seconds)",
                "Distance between predicted and real point (in meters)",
                chartDataset, PlotOrientation.VERTICAL,
                true,true,false);

        CategoryPlot plot = (CategoryPlot) lineChartObject.getPlot();
        plot.getDomainAxis().setCategoryLabelPositions(CategoryLabelPositions.UP_90);
        plot.getDomainAxis().setUpperMargin(DateAxis.DEFAULT_UPPER_MARGIN / 5);
        plot.getDomainAxis().setLowerMargin(DateAxis.DEFAULT_LOWER_MARGIN / 5);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinesVisible(true);

        int width = 1920;    // width of the image
        int height = 720;    // height of the image
        File lineChart = new File(outputChartPath);
        try {
            ChartUtils.saveChartAsJPEG(lineChart, lineChartObject, width ,height);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
