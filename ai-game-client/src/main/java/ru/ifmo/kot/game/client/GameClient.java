package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ClientEndpoint
public class GameClient {

    private static List<GameClient> CLIENTS = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private static final String GREETING = "The connection is open";
    private Session session;

    @OnOpen
    public void greetServer(final Session session) {
        this.session = session;
        try {
            session.getBasicRemote().sendText(GREETING);
            LOGGER.debug(GREETING);
        } catch (IOException e) {
            LOGGER.error("Failed to greet the server");
        }
    }

    @OnClose
    public void handleClose(final Session session) {
        LOGGER.debug("The connection was closed");
    }

    @OnMessage
    public void handleMessage(final String message) {
        LOGGER.info("The server: %s", message);
    }

    @OnError
    public void handleServerError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred on the server");
    }

    public void sendMessage(final String message) throws IOException {
        if (session.isOpen()) {
            session.getBasicRemote().sendText(message);
        } else {
            LOGGER.error("Failed to send message to the server");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.SECONDS.toMillis(30));
        try {
            webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL));
            TimeUnit.SECONDS.sleep(30);
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }

    }

}
