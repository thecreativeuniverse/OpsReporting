package net.cubeops.opsreporting.client.utils;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class Translatable {

    private static final String DEFAULT_LANG = "en_uk";

    public static String INFO_SENDING_REPORT;
    public static String SUCCESS_SENT_REPORT;
    public static String FAILED_REPORT_NOT_SENT;
    public static String INFO_GATHERING_DATA;
    public static String INFO_SPARK_NOT_ACTIVE;
    public static String ERROR_ONE_PER_SESSION;
    public static String ERROR_NO_KEY;

    public static void initialize(String lang) {
        File file = getFile(lang);
        if (!file.exists()) file = getDefaultFile();

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        INFO_SENDING_REPORT = translate(config.getString("sending-report"));
        SUCCESS_SENT_REPORT = translate(config.getString("sent-report"));
        FAILED_REPORT_NOT_SENT = translate(config.getString("failed-report-send"));
        INFO_SENDING_REPORT = translate(config.getString("gathering-data"));
        INFO_SPARK_NOT_ACTIVE = translate(config.getString("spark-not-active"));
        ERROR_ONE_PER_SESSION = translate(config.getString("one-per-session"));
        ERROR_ONE_PER_SESSION = translate(config.getString("no-key"));
    }


    private static File getFile(String fileName) {
        return new File(String.format("plugins%clang%c%s.yml", File.separatorChar, File.separatorChar, fileName));
    }

    private static File getDefaultFile() {
        File file = getFile(DEFAULT_LANG);
        if (!file.exists()) {
            try {
                System.out.println(file.getAbsolutePath()); //debugging
                file.createNewFile();
                String defaultFile = String.format("%clang%c%s.yml", File.separatorChar, File.separatorChar, DEFAULT_LANG); //debugging
                try (FileInputStream fi = new FileInputStream(defaultFile); FileOutputStream fo = new FileOutputStream(file)) {
                    int i;
                    while ((i = fi.read()) != -1) fo.write(i);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    private static String translate(String s) {
        return s == null ? "" : ChatColor.translateAlternateColorCodes('&', s);
    }

}
