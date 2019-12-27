package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBBridge {
    final private String dbURL = "jdbc:mysql://localhost:3306/edge_server_db?useSSL=false";
    final private String dbUser = "root";
    final private String dbPassword = "root";

    private Connection connection = null;

    public DBBridge() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found!");
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("Connection to the database failed!");
            e.printStackTrace();
        }
    }

    public void close(){
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

//    public boolean insertDatapoint(DBDatapoint datapoint) {
//        if (connection == null) return false;
//        String insertString = "INSERT INTO datpoints VALUES (default, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
//        try {
//            PreparedStatement statement = connection.prepareStatement(insertString);
//            statement.setDouble(1, datapoint.timestep);
//            statement.setInt(2, datapoint.device_id);
//            statement.setDouble(3, datapoint.real_lat);
//            statement.setDouble(4, datapoint.real_long);
//            statement.setDouble(5, datapoint.predicted_lat);
//            statement.setDouble(6, datapoint.predicted_long);
//            statement.setDouble(7, datapoint.real_RSSI);
//            statement.setDouble(8, datapoint.real_throughput);
//            statement.setDouble(9, datapoint.predicted_RSSI);
//            statement.setDouble(10, datapoint.predicted_throughput);
//            statement.executeUpdate();
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

    public boolean insertReal(double timestep, int device_id, double real_lat, double real_long, double real_RSSI, double real_throughput) {
        if (connection == null) return false;
        String insertString = "INSERT INTO datapoints (id, timestep, device_id, real_lat, real_long, real_RSSI, real_throughput) VALUES (default, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = connection.prepareStatement(insertString);
            statement.setDouble(1, timestep);
            statement.setInt(2, device_id);
            statement.setDouble(3, real_lat);
            statement.setDouble(4, real_long);
            statement.setDouble(5, real_RSSI);
            statement.setDouble(6, real_throughput);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean insertPredicted(double timestep, int device_id, double predicted_lat, double predicted_long, double predicted_RSSI, double predicted_throughput) {
        if (connection == null) return false;
        String insertString = "INSERT INTO datapoints (id, timestep, device_id, predicted_lat, predicted_long, predicted_RSSI, predicted_throughput) VALUES (default, ?, ?, ?, ?, ?, ?);";
        try {
            PreparedStatement statement = connection.prepareStatement(insertString);
            statement.setDouble(1, timestep);
            statement.setInt(2, device_id);
            statement.setDouble(3, predicted_lat);
            statement.setDouble(4, predicted_long);
            statement.setDouble(5, predicted_RSSI);
            statement.setDouble(6, predicted_throughput);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public boolean updateWithReal(double timestep, int device_id, double real_lat, double real_long, double real_RSSI, double real_throughput) {
        if (connection == null) return false;
        String updateString = "UPDATE datapoints SET real_lat = ?, real_long = ?, real_RSSI = ?, real_throughput = ? WHERE timestep = ? AND device_id = ?;";
        try {
            PreparedStatement statement = connection.prepareStatement(updateString);
            statement.setDouble(1, real_lat);
            statement.setDouble(2, real_long);
            statement.setDouble(3, real_RSSI);
            statement.setDouble(4, real_throughput);
            statement.setDouble(5, timestep);
            statement.setInt(6, device_id);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

//    public List<DBDatapoint> getTerminalRealPredictedLatLons(int terminalId) {
//        if (connection == null) return null;
//        String query = "SELECT * FROM datapoints WHERE device_id = ?;";
//        List<DBDatapoint> list = null;
//        try {
//            PreparedStatement statement = connection.prepareStatement(query);
//            statement.setInt(1, terminalId);
//            ResultSet resultSet = statement.executeQuery();
//            list = new ArrayList<>();
//            while (resultSet.next()) {
//                DBDatapoint newDatapoint = new DBDatapoint(
//                        resultSet.getDouble("real_lat"),
//                        resultSet.getDouble("real_long"),
//                        resultSet.getDouble("predicted_lat"),
//                        resultSet.getDouble("predicted_long"));
//                list.add(newDatapoint);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return null;
//        }
//        return list;
//    }

    public List<DBDatapoint> getTerminalRealPredictedLatLons(int terminalId) {
        if (connection == null) return null;
        String query = "SELECT * FROM datapoints WHERE device_id = ?;";
        List<DBDatapoint> list = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, terminalId);
            ResultSet resultSet = statement.executeQuery();
            list = new ArrayList<>();
            while (resultSet.next()) {
                DBDatapoint newDatapoint = new DBDatapoint(
                        resultSet.getDouble("real_lat"),
                        resultSet.getDouble("real_long"),
                        resultSet.getDouble("predicted_lat"),
                        resultSet.getDouble("predicted_long"));
                list.add(newDatapoint);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        return list;
    }

    public boolean truncateAllDatapoints() {
        if (connection == null) return false;
        String truncString = "TRUNCATE TABLE datapoints;";
        try {
            PreparedStatement statement = connection.prepareStatement(truncString);
            statement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
