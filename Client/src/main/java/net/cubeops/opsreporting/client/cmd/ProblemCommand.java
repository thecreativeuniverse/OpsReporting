package net.cubeops.opsreporting.client.cmd;

import net.cubeops.opsreporting.client.OpsReportingClient;
import net.cubeops.opsreporting.client.send.ReportBuilder;
import net.cubeops.opsreporting.client.utils.Translatable;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.List;

public class ProblemCommand implements CommandExecutor, TabCompleter {

    private boolean alreadySent = false;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (!sender.hasPermission(OpsReportingClient.PERMISSION)) {
            return false;
        }
        if (alreadySent) {
            sender.sendMessage(Translatable.ERROR_ONE_PER_SESSION);
            return true;
        }
        OpsReportingClient.broadcastInfo(Translatable.INFO_GATHERING_DATA, OpsReportingClient.PERMISSION);
        ReportBuilder.gatherInfo(String.join(" ", args));
        alreadySent = true;
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender.hasPermission(OpsReportingClient.PERMISSION)) return List.of("<brief description of issue>");
        else return null;
    }


}