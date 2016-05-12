package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.tools.EmbeddedLogger;
import ru.ifmo.kot.tools.Messenger;

import javax.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

    private static List<Session> CLIENTS = new ArrayList<>();
    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private static final String GREETING = "The connection is open";
    private Session session;

    @OnOpen
    public void greetServer(final Session session) {
        this.session = session;
        try {
            sendMessage(new Messenger.Message("Player", "Hello!"));
            LOGGER.debug(GREETING);
        } catch (Exception e) {
            LOGGER.error("Failed to greet the server");
        }
    }

    @OnClose
    public void handleClose(final Session session) {
        LOGGER.debug("The connection was closed");
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message) {
        LOGGER.info("The server %s: %s" , message.getPlayerName(), message.getContent());
    }

    @OnError
    public void handleServerError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred on the server");
    }

    private void sendMessage(final Messenger.Message message) throws IOException, EncodeException {
        if (session.isOpen()) {
            session.getBasicRemote().sendObject(message);
        } else {
            LOGGER.error("Failed to send message to the server");
        }
    }

    public static void main(String[] args) throws InterruptedException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.SECONDS.toMillis(30));
        try {
            CLIENTS.add(webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL)));
            TimeUnit.SECONDS.sleep(30);
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }

    }

}
