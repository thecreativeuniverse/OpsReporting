package net.cubeops.opsreporting.server.api;

import com.google.gson.JsonObject;
import net.cubeops.opsreporting.report.Report;
import net.cubeops.opsreporting.server.OpsReportingServer;
import net.cubeops.opsreporting.server.exception.VariableNotFoundException;
import net.cubeops.opsreporting.server.pushover.PushoverHandler;

import java.sql.*;
import java.util.Map;

public class MySQLHandler {

    public static void handleRequest(JsonObject obj) {
        String key = obj.get("key").getAsString();
        if (key == null) return;
        if (!obj.has("report")) return;

        Report report = Report.deserialize(obj.getAsJsonObject("report"));

        Connection connection = null;
        try {
            connection = getConnection();
            if (connection == null) {
                System.out.println("Could not connect to MySQL.");
                return;
            }
            Statement statement = connection.createStatement();
            ResultSet res = statement.executeQuery(String.format("select clientKey, clientName from clientKeys where clientKey = %s", key));
            if (res.next()) {
                String clientName = res.getString("clientName");
                new PushoverHandler(clientName, report).push();
            } else {
                System.out.printf("Key %s not found in MySQL database.%n", key);
            }
        } catch (VariableNotFoundException e) {
            printError(e, "settings.yml does not include valid mysql information.");
        } catch (SQLException e) {
            printError(e, "An error occurred connecting to MySQL.");
        } finally {
            try {
                if (connection != null) connection.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Connection getConnection() throws VariableNotFoundException, SQLException {

        Map<String, String> env = System.getenv();

        if (!env.containsKey(OpsReportingServer.SQL_USERNAME_KEY))
            throw new VariableNotFoundException("MySQL username has not been provided.");
        if (!env.containsKey(OpsReportingServer.SQL_PASSWORD_KEY))
            throw new VariableNotFoundException("MySQL password has not been provided.");
        if (!env.containsKey(OpsReportingServer.SQL_HOSTNAME_KEY))
            throw new VariableNotFoundException("MySQL host name has not been provided.");

        String url = env.get(OpsReportingServer.SQL_HOSTNAME_KEY);
        String username = env.get(OpsReportingServer.SQL_USERNAME_KEY);
        String password = env.get(OpsReportingServer.SQL_PASSWORD_KEY);

        return DriverManager.getConnection(url, username, password);

    }

    private static void printError(Exception e, String msg) {
        System.out.println(msg);
        e.printStackTrace();
    }

    public static boolean ping() {
        Connection connection = null;
        try {
            connection = getConnection();
            return connection != null;
        } catch (VariableNotFoundException | SQLException e) {
            printError(e, "An error occurred performing MySQL Health Check.");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

}