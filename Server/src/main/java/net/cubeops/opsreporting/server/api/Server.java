package net.cubeops.opsreporting.server.api;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.cubeops.opsreporting.server.OpsReportingServer;
import spark.Spark;

public class Server {

    public void start() {
        try {
            Spark.port(Integer.parseInt(System.getenv().get(OpsReportingServer.PORT_KEY)));
        } catch (NumberFormatException e) {
            System.out.println("Could not read port number from environment variable.");
            OpsReportingServer.stopServer();
            return;
        }
        Spark.initExceptionHandler(e -> {
            System.out.println("An error occurred whilst initialising server.");
            e.printStackTrace();
            OpsReportingServer.stopServer();
        });
        Spark.post("/api", (request, response) -> {
            response.type("application/json");
            int status = checkRequest(request.body());
            response.status(status);
            JsonObject obj = new JsonObject();
            obj.addProperty("status", status);
            return obj;
        });
        System.out.println("Connecting to server.");
    }

    public void stop() {
        Spark.stop();
        System.out.println("Closing server.");
    }

    private int checkRequest(String jsonString) {
        JsonObject obj = (JsonObject) JsonParser.parseString(jsonString);
        if (!obj.has("key")) return 400;

        //to handle health check requests
        if (!obj.has("report")) {
            String key = obj.get("key").getAsString();
            if (key.equalsIgnoreCase("healthcheck") && MySQLHandler.ping()) return 200;
            return 400;
        }

        new Thread(() -> MySQLHandler.handleRequest(obj)).start();

        return 202;
    }

}
