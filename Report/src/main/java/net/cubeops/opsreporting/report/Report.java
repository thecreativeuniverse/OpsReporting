package net.cubeops.opsreporting.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public record Report(String key, String serverName, Integer playerCount, double[] tps, double[] cpuSystem,
                     double[] cpuProcess, String memoryUsage, String diskUsage, long uptime, String sparkLink,
                     String description) implements Serializable {

    public CompletableFuture<Integer> send() {

        CompletableFuture<Integer> future = new CompletableFuture<>();

        new Thread(() -> {
            try {
                String webAddress = "https://issue-reporting-api.cubeops.net/api";
                URL url = new URL(webAddress);

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json; utf-8");
                connection.setRequestProperty("Accept", "application/json");
                connection.setDoOutput(true);
                connection.setConnectTimeout(5000);

                String post = serialize().toString();
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = post.getBytes();
                    os.write(input, 0, input.length);
                    os.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                    future.complete(400);
                }
                try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
                    StringBuilder sb = new StringBuilder();
                    String responseLine;
                    while ((responseLine = in.readLine()) != null) {
                        sb.append(responseLine);
                    }
                    JsonObject obj = (JsonObject) JsonParser.parseString(sb.toString());
                    future.complete(obj.get("status").getAsInt());
                } catch (IOException e) {
                    e.printStackTrace();
                    future.complete(400);
                }

            } catch (IOException e) {
                future.complete(400);
                e.printStackTrace();
            }

            future.complete(400);

        }).start();

        return future;
    }

    /*
    {
        "key" : (int)
        "report" : {
            "server-name" : (String)
            "player-count" : (int)
            "tps" : [
                double,
                double,
                double
            ]
            "uptime": (String)
            "spark": (String)
            "problem-description": (String)
        }
    }
     */
    private JsonObject serialize() {
        JsonObject toReturn = new JsonObject();

        JsonObject reportObj = new JsonObject();
        reportObj.addProperty("server-name", serverName);
        reportObj.addProperty("player-count", playerCount);
        JsonArray tpsArr = new JsonArray();
        if (tps != null) {
            for (double d : tps) {
                tpsArr.add(d);
            }
        }
        reportObj.add("tps", tpsArr);
        JsonArray sysArr = new JsonArray();
        if (cpuSystem != null) {
            for (double d : cpuSystem) {
                sysArr.add(d);
            }
        }
        reportObj.add("cpu-system", sysArr);
        JsonArray processArr = new JsonArray();
        if (cpuProcess != null) {
            for (double d : cpuProcess) {
                processArr.add(d);
            }
        }
        reportObj.add("cpu-process", processArr);

        reportObj.addProperty("memory-usage", memoryUsage);
        reportObj.addProperty("disk-usage", diskUsage);
        reportObj.addProperty("uptime", uptime);
        reportObj.addProperty("spark", sparkLink == null ? "Not provided" : sparkLink);
        reportObj.addProperty("problem-description", description == null ? "Not provided" : description);

        toReturn.addProperty("key", key);
        toReturn.add("report", reportObj);

        return toReturn;
    }

    public static Report deserialize(JsonObject object) {
        String serverName = object.get("server-name").getAsString();
        int playerCount = object.get("player-count").getAsInt();
        JsonArray tpsArr = object.getAsJsonArray("tps");
        double[] tps = new double[tpsArr.size()];
        for (int i = 0; i < tpsArr.size(); i++) {
            tps[i] = tpsArr.get(i).getAsDouble();
        }
        JsonArray sysArr = object.getAsJsonArray("cpu-system");
        double[] cpuSys = new double[sysArr.size()];
        for (int i = 0; i < sysArr.size(); i++) {
            cpuSys[i] = sysArr.get(i).getAsDouble();
        }
        JsonArray procArr = object.getAsJsonArray("cpu-process");
        double[] cpuProcess = new double[procArr.size()];
        for (int i = 0; i < procArr.size(); i++) {
            cpuProcess[i] = procArr.get(i).getAsDouble();
        }
        String memoryUsage = object.get("memory-usage").getAsString();
        String diskUsage = object.get("disk-usage").getAsString();
        long uptime = object.get("uptime").getAsLong();
        String sparkLink = object.get("spark").getAsString();
        String desc = object.get("problem-description").getAsString();

        return new Report(
                null,
                serverName,
                playerCount,
                tps,
                cpuSys,
                cpuProcess,
                memoryUsage,
                diskUsage,
                uptime,
                sparkLink,
                desc
        );
    }

    public static String uptimeAsString(long uptime) {
        long hours = TimeUnit.MILLISECONDS.toHours(uptime);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(uptime - TimeUnit.HOURS.toMillis(hours));
        return String.format("%d Hours, %d Minutes", hours, minutes);
    }

    /*
    server-name:
    player-count:
    uptime:
    tps-average:
    cpu-system:
    cpu-process:
    memory-usage:
    disk-usage:
    spark:
    problem-description:
    */
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Server name: %s%n", serverName()));
        sb.append(String.format("Player count: %s%n", playerCount()));
        sb.append(String.format("Uptime: %s%n", uptimeAsString(uptime())));
        sb.append(String.format("TPS averages: %s%n", arrayToString(tps())));
        sb.append(String.format("CPU usage (system): %s%n", arrayToString(cpuSystem())));
        sb.append(String.format("CPU usage (process): %s%n", arrayToString(cpuProcess())));
        sb.append(String.format("Memory usage: %s%n", memoryUsage()));
        sb.append(String.format("Disk usage: %s%n", diskUsage()));
        sb.append(String.format("Spark link: %s%n", sparkLink()));
        sb.append(String.format("Problem description: %s%n", description()));
        return sb.toString();
    }

    private String arrayToString(double[] arr) {
        if (arr.length == 0) return "Not provided";
        StringBuilder sb = new StringBuilder();
        for (double val : arr) {
            sb.append(String.format("%f, ", val));
        }
        return sb.toString();
    }

}