package net.cubeops.opsreporting.server.pushover;

import de.svenkubiak.jpushover.JPushover;
import de.svenkubiak.jpushover.enums.Priority;
import de.svenkubiak.jpushover.exceptions.JPushoverException;
import de.svenkubiak.jpushover.http.PushoverResponse;
import net.cubeops.opsreporting.report.Report;
import net.cubeops.opsreporting.server.OpsReportingServer;
import net.cubeops.opsreporting.server.exception.VariableNotFoundException;

import java.util.Map;

public class PushoverHandler {

    private final String clientName;
    private final Report report;
    private final String token;
    private final String userKey;

    public PushoverHandler(String clientName, Report report) throws VariableNotFoundException {
        this.clientName = clientName;
        this.report = report;
        Map<String, String> env = System.getenv();
        if (env.containsKey(OpsReportingServer.PUSHOVER_TOKEN_KEY))
            this.token = env.get(OpsReportingServer.PUSHOVER_TOKEN_KEY);
        else throw new VariableNotFoundException("Pushover token has not been provided.");

        if (env.containsKey(OpsReportingServer.PUSHOVER_USER_KEY))
            this.userKey = env.get(OpsReportingServer.PUSHOVER_USER_KEY);
        else throw new VariableNotFoundException("Pushover user key has not been provided.");
    }

    public void push() {
        try {
            PushoverResponse response = JPushover.messageAPI()
                    .withToken(token)
                    .withUser(userKey)
                    .withMessage(report.toString())
                    .withTitle(String.format("Ops Reporting: %s", clientName))
                    .withPriority(Priority.LOWEST)
                    .push();

            if (response.getHttpStatus() != 200) {
                System.out.println("An error occurred sending pushover notification. HTTP Status: " + response.getHttpStatus());
                System.out.println(response.getResponse());
            }
        } catch (JPushoverException e) {
            System.out.println("Could not send pushover notification.");
            e.printStackTrace();
        }
    }

}
