package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import ru.ifmo.kot.game.elements.Field;
import ru.ifmo.kot.game.elements.Player;
import ru.ifmo.kot.game.visualiztion.VisualizationEndpoint;
import ru.ifmo.kot.tools.ApiCommands;
import ru.ifmo.kot.tools.EmbeddedLogger;
import ru.ifmo.kot.tools.Messenger;
import ru.ifmo.kot.tools.Response;

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
import java.util.Set;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(
        value = "/game",
        encoders = {Messenger.MessageEncoder.class},
        decoders = {Messenger.MessageDecoder.class})
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private static final Game game = new Game();
    private static final List<Session> clients = new ArrayList<>(ServerConstants.MAX_NUM_OF_CLIENTS);

    private Session clientSession;


    public static void main(String[] args) {
        Log.setLog(new EmbeddedLogger());
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
            LOGGER.info("The game started.");
            server.join();
        } catch (Throwable e) {
            LOGGER.info("The game cannot started.");
        }
    }

    @OnOpen
    public void addClient(final Session session) {
        if (clients.size() < ServerConstants.MAX_NUM_OF_CLIENTS) {
            clients.add(session);
            clientSession = session;
            LOGGER.debug("The clientSession %s was added successfully", session.getId());
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
        LOGGER.debug("The client session %s was removed successfully", session.getId());
    }

    @SuppressWarnings("UnusedParameters")
    @OnError
    public void handleClientError(final Session session, final Throwable error) {
        LOGGER.error("An error occurred on the %s client session", session.getId());
        removeClient(session);
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    @OnMessage
    public void handleMessage(final Messenger.Message message, final Session session) {
        final Object[] args = message.getArgs();
        switch (message.getCommand()) {
            case ApiCommands.WEIGHT:
                final String vrtx1 = (String) args[0];
                final String vrtx2 = (String) args[1];
                final int weight = game.weight(vrtx1, vrtx2);
                sendMessage( ApiCommands.WEIGHT, weight);
                break;
            case ApiCommands.NAME:
                final String playerName = (String) args[0];
                if (game.addPlayer(playerName)) {
                    LOGGER.info("The player was added successfully as %s", playerName);
                    sendMessage(ApiCommands.NAME, Response.OK);
                    sendMessage(ApiCommands.START_DATA, game.startVertices());
                } else {
                    LOGGER.info("Failed to add the player");
                    sendMessage(ApiCommands.NAME, Response.FAIL);
                }
                break;
            case ApiCommands.NEXT_VERTICES:
                final String currentVertexName = (String) args[0];
                final Set<String> nextVerticesSet = game.nextVertices(currentVertexName);
                String[] nextVertices = new String[nextVerticesSet.size()];
                nextVerticesSet.toArray(nextVertices);
                sendMessage(ApiCommands.NEXT_VERTICES, nextVertices);
                break;
            case ApiCommands.MOVE:
                final String nextVertexName = (String) args[0];
                game.move(message.getParticipant(), nextVertexName);
                sendMessage(ApiCommands.MOVE, Response.OK);
                break;
            default:
                LOGGER.error("The %s command: %s", message.getParticipant(),
                        ApiCommands.UNRECOGNIZABLE);
        }
    }

    private void sendMessage(final Messenger.Message message) {
        if (clientSession.isOpen()) { // todo check the need
            try {
                clientSession.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                LOGGER.error("Failed to send a message to the client");
            }
        } else {
            removeClient(clientSession);
        }
    }

    private void sendMessage(final String command, final Object... args) {
        sendMessage(new Messenger.Message("server", command, args));
    }

    private static class Game {

        private final Field field = new Field();

        boolean addPlayer(final String playerName) {
            return Player.addPlayer(playerName);
        }

        String[] startVertices() {
            return field.getStartVertices();
        }

        int weight(final String vertex1, final String vertex2) {
            return field.getGameModel().getWeight(vertex1, vertex2);
        }

        Set<String> nextVertices(final String vertex) {
            return field.getNextVertices(vertex);
        }

        boolean move(final String playerName, final String vertexName) {
            final Player player = Player.getPlayer(playerName);
            player.setCurrentPosition(vertexName);
            return true;
        }

    }

}
