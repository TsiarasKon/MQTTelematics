package com.example.androidterminal.edge_server;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class EdgeServer {
    private static final String ipAddr = "85.72.152.109";
    private static final int port = 1883;
    private static final boolean fullDateFormat = false;

    private static final String[] vehicleTopics = {"v26/topic", "v27/topic"};

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
            return vehicleTopics[index];
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
