package net.cubeops.opsreporting.client.send;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.lucko.spark.api.Spark;
import net.cubeops.opsreporting.client.OpsReportingClient;
import net.cubeops.opsreporting.report.Report;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;

public class ReportBuilder {

    private static final int SPARK_TIMEOUT = 600;

    public static void gatherInfo(String args) {

        runAsync(() -> {
            Server server = Bukkit.getServer();

            int playerCount = server.getOnlinePlayers().size();
            String memoryUsage = getMemory();
            String diskUsage = getDiskUsage();

            try {
                Class.forName("me.lucko.spark.api.Spark");
                RegisteredServiceProvider<Spark> provider = Bukkit.getServicesManager().getRegistration(Spark.class);
                if (provider != null) {
                    run(() -> server.dispatchCommand(server.getConsoleSender(), String.format("spark sampler --timeout %d", SPARK_TIMEOUT)));
                    Spark spark = provider.getProvider();
                    double[] tps = spark.tps() == null ? null : spark.tps().poll();
                    double[] cpuSystem = spark.cpuSystem().poll();
                    double[] cpuProcess = spark.cpuProcess().poll();
                    Bukkit.getScheduler().runTaskLaterAsynchronously(OpsReportingClient.instance,
                            () -> sendInfo(playerCount, tps, cpuSystem, cpuProcess, memoryUsage, diskUsage, true, args), (SPARK_TIMEOUT + 10) * 20L);
                    return;
                }
            } catch (ClassNotFoundException e) { //will throw this if spark is not loaded
                OpsReportingClient.info("Spark plugin is not active. CubeOps problem report will be sent without certain information.");
            }
            sendInfo(playerCount, null, null, null, memoryUsage, diskUsage, false, args);
        });

    }

    private static void sendInfo(int playerCount, double[] tps, double[] cpuSystem, double[] cpuProcess, String memoryUsage, String diskUsage, boolean spark, String description) {

        String sparkLink = null;
        if (spark) {
            RegisteredServiceProvider<Spark> provider = Bukkit.getServicesManager().getRegistration(Spark.class);
            if (provider != null) {
                try {
                    String sparkFolderPath = String.format("%s%c%s", provider.getPlugin().getDataFolder().getPath(), File.separatorChar, "activity.json");
                    File file = new File(sparkFolderPath);
                    FileReader reader = new FileReader(file);
                    JsonParser parser = new JsonParser();
                    Object obj = parser.parse(reader);

                    JsonArray list = (JsonArray) obj;
                    JsonObject mostRecent = list.get(list.size() - 1).getAsJsonObject();
                    JsonObject dataObject = mostRecent.getAsJsonObject("data");
                    sparkLink = dataObject.get("value").getAsString();
                } catch (FileNotFoundException e) {
                }
            }
        }
        if (OpsReportingClient.KEY == null) {
            OpsReportingClient.warn("No OpsReporting key provided. Please check config.yml");
            return;
        }

        String serverName;
        try {
            BufferedReader reader = new BufferedReader(new FileReader("server.properties"));
            Properties properties = new Properties();
            properties.load(reader);
            reader.close();

            serverName = properties.getProperty("server-name", "Not Found");

        } catch (IOException e) {
            e.printStackTrace();
            serverName = "Not Found";
        }
        Report report = new Report(
                OpsReportingClient.KEY,
                serverName,
                playerCount,
                tps,
                cpuSystem,
                cpuProcess,
                memoryUsage,
                diskUsage,
                System.currentTimeMillis() - OpsReportingClient.UPTIME_START,
                sparkLink,
                description
        );

        OpsReportingClient.info("Sending server problem report to CubeOps...");
        report.send().thenAcceptAsync(status -> {
            if (status == 202) OpsReportingClient.info("Sent server problem report to CubeOps.");
            else OpsReportingClient.warn("Unable to send server problem report to CubeOps.");
        });

    }

    private static void run(Runnable runnable) {
        Bukkit.getScheduler().runTask(OpsReportingClient.instance, runnable);
    }

    private static void runAsync(Runnable runnable) {
        Bukkit.getScheduler().runTaskAsynchronously(OpsReportingClient.instance, runnable);
    }

    private static String getMemory() {
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        return String.format("%s / %s", bytesToGigaBytes(heapUsage.getUsed()), bytesToGigaBytes(heapUsage.getMax()));
    }

    private static String getDiskUsage() {
        try {
            FileStore store = Files.getFileStore(Paths.get("."));
            if (store == null) return null;
            return String.format("%s / %s", bytesToGigaBytes(store.getTotalSpace() - store.getUsableSpace()), bytesToGigaBytes(store.getTotalSpace()));
        } catch (IOException e) {
            return null;
        }
    }

    private static String bytesToGigaBytes(long bytes) {
        int index = (int) Math.floor(Math.log(bytes) / Math.log(1024));
        String[] sizes = new String[]{"B", "KB", "MB", "GB", "TB"};

        return String.format("%.1f %s", (bytes / Math.pow(1024, index)), sizes[index]);
    }

}
