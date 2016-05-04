package ru.ifmo.kot.game.client;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static ru.ifmo.kot.game.client.ClientConstants.SERVER_URL;

public class MainClient {

    static { Log.setLog(new SimplestLogger()); }
    private static final Logger LOGGER = Log.getLogger("SimplestLogger");
    static { LOGGER.setDebugEnabled(false); }


    public static void main(String[] args) {
        final HttpClient client = new HttpClient();
        try {
            client.GET(SERVER_URL);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            LOGGER.debug("The client cannot send the request");
        }
    }

}
