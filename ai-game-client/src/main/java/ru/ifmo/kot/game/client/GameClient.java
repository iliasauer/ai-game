package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.*;

import java.io.IOException;
import java.net.URI;

@ClientEndpoint
public class GameClient {

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

    public static void main(String[] args) {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        Session session = null;
        try {
             session = webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL));
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }

    }

}
