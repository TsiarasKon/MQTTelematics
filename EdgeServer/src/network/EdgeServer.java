package network;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class EdgeServer {
    private static final String ipAddr = "127.0.0.1";
    private static final int port = 1883;
    private static final boolean fullDateFormat = false;

    private static final String[] vehicle2esTopics = {"v26_ES/topic", "v27_ES/topic"};
    private static final String[] es2vehicleTopics = {"ES_v26/topic", "ES_v27/topic"};     // unused for now

    private EdgeServer() {      // we don't want instances of this class
    }

    public static String getIpAddr() {
        return ipAddr;
    }

    public static int getPort() {
        return port;
    }

    public static String getBroker() {
        return "tcp://" + ipAddr + ':' + port;
    }

    public static String getVehicleTopic(int index) {
        try {
            return vehicle2esTopics[index];
        } catch (IndexOutOfBoundsException e) {
            System.err.println("Tried to getVehicleTopic(index) with invalid index!");
            return null;
        }
    }

    public static String getCurrentTime() {
        String datePattern = fullDateFormat ? "yyyy-MM-dd HH:mm:ss z" : "HH:mm:ss";
        SimpleDateFormat formatter = new SimpleDateFormat(datePattern);
        Date date = new Date(System.currentTimeMillis());
        return formatter.format(date);
    }
}
