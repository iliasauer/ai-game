package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.tools.Messenger;

import javax.websocket.*;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private static final String GREETING = "The connection is open";
    private Session session;

    @OnOpen
    public void greetServer(final Session session) {
        this.session = session;
        try {
            sendMessage(new Messenger.Message("player1", "weight", "Moscow", "Novosibirsk"));
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
        if (message.getCommand().equals("weight")) {
            final Integer response = (Integer) message.getArgs()[0];
            LOGGER.info("The %s command %s: %d" , message.getPlayerName(), message.getCommand(),
                    response);
        }

    }

    @OnError
    public void handleError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred");
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
            final Session server = webSocketContainer.connectToServer(GameClient.class,
                URI.create(ClientConstants.SERVER_URL)
            ); // todo check is it the same session
            TimeUnit.SECONDS.sleep(30);
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }

    }

}
