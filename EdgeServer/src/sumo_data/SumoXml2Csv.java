package sumo_data;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Locale;
import java.util.Random;

public class SumoXml2Csv {
    private double min_lat;
    private double max_lat;
    private double min_lon;
    private double max_lon;
    private int minRSSI = 20;
    private int maxRSSI = 100;
    private int meanRSSI = 60;
    private int stdRSSI = 100;      // change this to widen the margin between values
    private int maxLinkCapacity = 50;     // in Mbps

    private Random random;

    public SumoXml2Csv(double min_lat, double max_lat, double min_lon, double max_lon) {
        this.min_lat = min_lat;
        this.max_lat = max_lat;
        this.min_lon = min_lon;
        this.max_lon = max_lon;
        random = new Random();
    }

    public double getMin_lat() {
        return min_lat;
    }

    public void setMin_lat(double min_lat) {
        this.min_lat = min_lat;
    }

    public double getMax_lat() {
        return max_lat;
    }

    public void setMax_lat(double max_lat) {
        this.max_lat = max_lat;
    }

    public double getMin_lon() {
        return min_lon;
    }

    public void setMin_lon(double min_lon) {
        this.min_lon = min_lon;
    }

    public double getMax_lon() {
        return max_lon;
    }

    public void setMax_lon(double max_lon) {
        this.max_lon = max_lon;
    }

    public int getMeanRSSI() {
        return meanRSSI;
    }

    public void setMeanRSSI(int meanRSSI) {
        this.meanRSSI = meanRSSI;
    }

    public int getStdRSSI() {
        return stdRSSI;
    }

    public void setStdRSSI(int stdRSSI) {
        this.stdRSSI = stdRSSI;
    }

    public int getMaxLinkCapacity() {
        return maxLinkCapacity;
    }

    public void setMaxLinkCapacity(int maxLinkCapacity) {
        this.maxLinkCapacity = maxLinkCapacity;
    }

    private boolean latLonInBounds(double lat, double lon) {
        return (lat >= min_lat && lat <= max_lat && lon >= min_lon && lon <= max_lon);
    }

    private int getRSSI() {
        int gNum;
        do {        // keep trying to get a value between the RSSI limits
            gNum = (int) (random.nextGaussian() * stdRSSI) + meanRSSI;
        } while (gNum < minRSSI || gNum > maxRSSI);
        return gNum;
    }

    private double getThroughput(int currRSSI) {
        return (currRSSI / 100.0) * maxLinkCapacity;
    }

    public void xml2csvConvert(String xmlFilepath, String csvFilepath) {
        FileWriter csvWriter = null;
        try {
            csvWriter = new FileWriter(csvFilepath);
            File xmlVehiclesFile = new File(xmlFilepath);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document xmlDoc = dBuilder.parse(xmlVehiclesFile);
            xmlDoc.getDocumentElement().normalize();

            String expression = "/fcd-export/timestep";
            XPath xPath =  XPathFactory.newInstance().newXPath();
            NodeList timestepsList = (NodeList) xPath.compile(expression).evaluate(xmlDoc, XPathConstants.NODESET);
            for (int i = 0; i < timestepsList.getLength(); i++) {
                Node tsNode = timestepsList.item(i);
                if (tsNode.getNodeType() == Node.ELEMENT_NODE) {
                    Element tsElement = (Element) tsNode;
                    NodeList vehiclesList = tsElement.getElementsByTagName("vehicle");
                    Node vNode;
                    Element vElement;
                    double lat, lon;
                    int currRSSI;
                    for (int j = 0; j < vehiclesList.getLength(); j++) {
                        vNode = vehiclesList.item(j);
                        if (vNode.getNodeType() == Node.ELEMENT_NODE) {
                            vElement = (Element) vNode;
                            lat = Double.valueOf(vElement.getAttribute("y"));
                            lon = Double.valueOf(vElement.getAttribute("x"));
                            if (! latLonInBounds(lat, lon)) continue;
                            currRSSI = getRSSI();
                            csvWriter.append(tsElement.getAttribute("time") + ',' + vElement.getAttribute("id")
                                    + ',' + lat + ',' + lon + ',' + vElement.getAttribute("angle") + ',' +
                                    vElement.getAttribute("speed") + ',' + currRSSI + ','
                                    + String.format(Locale.US, "%.1f", getThroughput(currRSSI)) + '\n');
                        }
                    }
                }
            }
            csvWriter.flush();
            csvWriter.close();
        } catch (ParserConfigurationException | SAXException | XPathExpressionException e) {
            System.err.println("Encountered an unexpected error in '" + xmlFilepath + "'");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Encountered an unexpected error generating '" + csvFilepath + "'");
            e.printStackTrace();
        } finally {
            if (csvWriter != null) {
                try {
                    csvWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
