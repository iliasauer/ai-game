package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import javax.websocket.*;
import javax.websocket.server.ServerContainer;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(value = "/gameserver")
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private List<Session> clients = new ArrayList<>(ServerConstants.MAX_NUM_OF_CLIENTS);


    public static void main(String[] args) {
//        Log.setLog(new EmbeddedLogger());
        final Server server = new Server(PORT);
//        final WebAppContext context = new WebAppContext();
//        context.setContextPath(CONTEXT_PATH);
//        context.setResourceBase(WEBAPP_PATH);
//        context.setDescriptor(WEBXML_PATH);
//        context.setParentLoaderPriority(true);
//        server.setHandler(context);
        try {
            ServletContextHandler context =
                    new ServletContextHandler(ServletContextHandler.SESSIONS);
            context.setContextPath(CONTEXT_PATH);
            server.setHandler(context);
            // Initialize the JSR-356 layer
            final ServerContainer container =
                    WebSocketServerContainerInitializer.configureContext(context);
            container.addEndpoint(GameServer.class);
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

    @OnError
    public void handleClientError(final Session session, final Throwable error) {
        LOGGER.error("An error occurred on the %s client", session.getId());
        removeClient(session);
    }

    @OnMessage
    public void handleMessage(final Session session, final String message) {
        LOGGER.info("The client %s: %s", session.getId(), message);
    }

    public void sendMessage(final String message) throws IOException {
        for (final Session session: clients) {
            if (session.isOpen()) { // todo check the need
                session.getBasicRemote().sendText(message);
            } else {
                removeClient(session);
            }
        }
    }

}
