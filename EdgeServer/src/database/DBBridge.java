package database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBBridge {
    final private String dbURL = "jdbc:mysql://localhost:3306/edge_server_db?useSSL=false&allowPublicKeyRetrieval=true";
    final private String dbUser = "root";
    final private String dbPassword = "root";

    private Connection connection = null;

    public DBBridge() throws ClassNotFoundException, SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection(dbURL, dbUser, dbPassword);
        } catch (ClassNotFoundException e) {
            System.err.println("JDBC Driver not found!");
            e.printStackTrace();
            throw e;
        } catch (SQLException e) {
//            System.err.println("Connection to the database failed!");
//            e.printStackTrace();
            throw e;
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

    public boolean datapointExists(int terminalId, double timestep) {
        if (connection == null) return false;
        String query = "SELECT * FROM datapoints WHERE device_id = ? AND timestep = ?;";
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, terminalId);
            statement.setDouble(2, timestep);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<RealPredictedLatLon> getTerminalRealPredictedLatLons(int terminalId) {
        if (connection == null) return null;
        String query = "SELECT real_lat, real_long, predicted_lat, predicted_long FROM datapoints WHERE device_id = ?;";
        List<RealPredictedLatLon> list = null;
        try {
            PreparedStatement statement = connection.prepareStatement(query);
            statement.setInt(1, terminalId);
            ResultSet resultSet = statement.executeQuery();
            list = new ArrayList<>();
            while (resultSet.next()) {
                RealPredictedLatLon newDatapoint = new RealPredictedLatLon(
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
