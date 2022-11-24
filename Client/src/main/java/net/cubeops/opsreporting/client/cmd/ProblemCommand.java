package net.cubeops.opsreporting.client.cmd;

import net.cubeops.opsreporting.client.OpsReportingClient;
import net.cubeops.opsreporting.client.send.ReportBuilder;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ProblemCommand implements CommandExecutor {

    private boolean alreadySent = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission(OpsReportingClient.PERMISSION)) {
            return false;
        }
        if (alreadySent) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&cYou can only send one problem report per server session."));
            return true;
        }
        OpsReportingClient.info("Gathering info for a server problem report. A report will be sent to CubeOps shortly.");
        ReportBuilder.gatherInfo(String.join(" ", args));
        alreadySent = true;
        return true;
    }

}