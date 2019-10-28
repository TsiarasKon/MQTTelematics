package sumo_data_handler;

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

public class SumoXml2csv {
    private static double min_lat = 37.9668800;
    private static double max_lat = 37.9686200;
    private static double min_lon = 23.7647600;
    private static double max_lon = 23.7753900;

    private static boolean latLonInBounds(double lat, double lon) {
        return (lat >= min_lat && lat <= max_lat && lon >= min_lon && lon <= max_lon);
    }

    public static void xml2csvConvert(String xmlFilepath, String outputDirpath) {
        String csvFilepath = outputDirpath + xmlFilepath.substring(xmlFilepath.lastIndexOf('/') + 1, xmlFilepath.lastIndexOf('.')) + ".csv";
        try {
            FileWriter csvWriter = new FileWriter(csvFilepath);
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
                    for (int j = 0; j < vehiclesList.getLength(); j++) {
                        vNode = vehiclesList.item(j);
                        if (vNode.getNodeType() == Node.ELEMENT_NODE) {
                            vElement = (Element) vNode;
                            lat = Double.valueOf(vElement.getAttribute("y"));
                            lon = Double.valueOf(vElement.getAttribute("x"));
                            if (! latLonInBounds(lat, lon)) continue;
                            // TODO: generate RSSI, throughput
                            csvWriter.append(tsElement.getAttribute("time") + ',' +
                                    vElement.getAttribute("id") + ',' + lat + ',' + lon + ',' +
                                    vElement.getAttribute("angle") + ',' +
                                    vElement.getAttribute("speed") + ",RSSI,throughput\n");
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
        }
    }
}
