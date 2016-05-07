package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.webapp.WebAppContext;
import ru.ifmo.kot.tools.EmbeddedLogger;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static ru.ifmo.kot.game.server.ServerConstants.*;

@ServerEndpoint("/gameserver")
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private List<Session> clients = new ArrayList<>(ServerConstants.MAX_NUM_OF_CLIENTS);


    public static void main(String[] args) {
        Log.setLog(new EmbeddedLogger());
        final Server server = new Server(PORT);
        final WebAppContext context = new WebAppContext();
        context.setContextPath(CONTEXT_PATH);
        context.setResourceBase(WEBAPP_PATH);
        context.setDescriptor(WEBXML_PATH);
        context.setParentLoaderPriority(true);
        server.setHandler(context);
        try {
            server.start();
            LOGGER.debug("The game server started.");
            server.join();
        } catch (Exception e) {
            LOGGER.debug("The game server cannot started.", e);
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
                LOGGER.debug("Failed to close the session");
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
        LOGGER.debug("An error occurred on the %s client", session.getId());
        removeClient(session);
    }

    @OnMessage
    public void handleMessage(final Session session, final String message) {
        LOGGER.info("The client %s: %s", session.getId(), message);
    }

    public void sendMessage(final String message) {
        for (final Session session: clients) {
            if (session.isOpen()) { // todo check the need
                try {
                    session.getBasicRemote().sendText(message);
                } catch (IOException e) {
                    LOGGER.debug("Failed to send the message to the client");
                }
            } else {
                removeClient(session);
            }
        }
    }

}
