package net.cubeops.opsreporting.client;

import net.cubeops.opsreporting.client.cmd.ProblemCommand;
import net.cubeops.opsreporting.client.utils.Translatable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
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

        Translatable.initialize(getConfig().getString("language", "en_uk"));

        ProblemCommand cmd = new ProblemCommand();
        getCommand("serverproblem").setExecutor(cmd);
        getCommand("serverproblem").setTabCompleter(cmd);
    }

    public static void broadcastInfo(String msg, String permission) {
        info(msg);
        broadcastToPlayers(msg, permission);
    }

    public static void broadcastWarning(String msg, String permission) {
        warn(msg);
        broadcastToPlayers(msg, permission);
    }

    private static void info(String info) {
        instance.getLogger().info(info);
    }

    private static void warn(String warn) {
        instance.getLogger().warning(warn);
    }

    private static void broadcastToPlayers(String msg, String permission) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (!p.hasPermission(permission)) continue;
            p.sendMessage(msg);
        }
    }


}