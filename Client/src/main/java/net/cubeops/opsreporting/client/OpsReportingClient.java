package net.cubeops.opsreporting.client;

import net.cubeops.opsreporting.client.cmd.ProblemCommand;
import net.cubeops.opsreporting.client.cmd.ProblemTabComplete;
import org.bukkit.plugin.java.JavaPlugin;

public class OpsReportingClient extends JavaPlugin {

    public static String PERMISSION = "cubeops.serverproblem";
    public static String KEY = null;
    public static long UPTIME_START;

    public static OpsReportingClient instance;

    public void onEnable() {
        instance = this;
        UPTIME_START = System.currentTimeMillis();
        KEY = getConfig().getString("key");

        getCommand("serverproblem").setExecutor(new ProblemCommand());
        getCommand("serverproblem").setTabCompleter(new ProblemTabComplete());

    }

    public static void info(String info) {
        instance.getLogger().info(info);
    }

    public static void warn(String warn) {
        instance.getLogger().warning(warn);
    }

}