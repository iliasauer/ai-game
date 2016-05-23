package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import ru.ifmo.kot.game.visualiztion.VisualizationEndpoint;
import ru.ifmo.kot.tools.Messenger;

import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(
        value = "/game",
        encoders = {Messenger.MessageEncoder.class},
        decoders = {Messenger.MessageDecoder.class})
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private final List<Session> clients = new ArrayList<>(ServerConstants.MAX_NUM_OF_CLIENTS);


    public static void main(String[] args) {
//        Log.setLog(new EmbeddedLogger());
        final Server server = new Server(PORT);
        try {
            ServletContextHandler context =
                    new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath(CONTEXT_PATH);
            server.setHandler(context);
            final ServerContainer container =
                    WebSocketServerContainerInitializer.configureContext(context);
            container.addEndpoint(GameServer.class);
            container.addEndpoint(VisualizationEndpoint.class);
            server.start();
            LOGGER.debug("The game server started.");
            server.join();
        } catch (Throwable e) {
            LOGGER.debug("The game server cannot started.");
        }
    }


    @OnOpen
    public void addClient(final Session session) {
        if (clients.size() < ServerConstants.MAX_NUM_OF_CLIENTS) {
            clients.add(session);
            LOGGER.debug("The client %s was added successfully", session.getId());
        } else {
            try {
                session.close();
                LOGGER.debug("Exceeded the maximal number of clients");
            } catch (IOException exception) {
                LOGGER.error("Failed to close the session");
            }
        }
    }

    @OnClose
    public void removeClient(final Session session) {
        clients.remove(session);
        LOGGER.debug("The client %s was removed successfully", session.getId());
    }

    @SuppressWarnings("UnusedParameters")
    @OnError
    public void handleClientError(final Session session, final Throwable error) {
        LOGGER.error("An error occurred on the %s client", session.getId());
        removeClient(session);
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message, final Session session) {
        LOGGER.info("The client %s: %s", message.getPlayerName(), message.getContent());
        for (final Session client: clients) {
            try {
                sendMessage(message);
            } catch (final Exception e) {
                LOGGER.error("Failed to send the message to a client");
            }
        }
    }

    private void sendMessage(final Messenger.Message message) throws IOException, EncodeException {
        for (final Session session: clients) {
            if (session.isOpen()) { // todo check the need
                session.getBasicRemote().sendObject(message);
            } else {
                removeClient(session);
            }
        }
    }

}
