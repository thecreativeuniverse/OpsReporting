package net.cubeops.opsreporting.server;

import net.cubeops.opsreporting.server.api.Server;

import java.io.File;

public class OpsReportingServer {

    private static Server SERVER;
    private static File configFile;

    public static String ADDRESS_KEY = "opsreporting_bind_address";
    public static String PORT_KEY = "opsreporting_bind_port";
    public static String SQL_USERNAME_KEY = "opsreporting_mysql_username";
    public static String SQL_PASSWORD_KEY = "opsreporting_mysql_password";
    public static String SQL_HOSTNAME_KEY = "opsreporting_mysql_hostname";
    public static String SQL_PORT_KEY = "opsreporting_mysql_port";
    public static String PUSHOVER_TOKEN_KEY = "opsreporting_pushover_key";
    public static String PUSHOVER_USER_KEY = "opsreporting_pushover_userkey";

    public static void main(String[] args) {
        SERVER = new Server();
        SERVER.start();
    }

    public static File getConfigFile() {
        return configFile;
    }

    public static void stopServer() {
        SERVER.stop();
        System.exit(0);
    }

    public static Server getServer() {
        return SERVER;
    }

}
