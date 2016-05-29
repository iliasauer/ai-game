package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import ru.ifmo.kot.game.Api;
import ru.ifmo.kot.game.elements.Field;
import ru.ifmo.kot.game.elements.Player;
import ru.ifmo.kot.game.visualiztion.VisualizationEndpoint;
import ru.ifmo.kot.tools.Commands;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.MAX_NUM_OF_CLIENTS;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(
        value = "/GAME",
        encoders = {Messenger.MessageEncoder.class},
        decoders = {Messenger.MessageDecoder.class})
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private static final Game GAME = new Game();
//    private static volatile boolean finishFlag = false;
    private static final Map<Session, String> CLIENTS = new HashMap<>(MAX_NUM_OF_CLIENTS);

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
            LOGGER.info("The game server is started and waiting for players.");
            server.join();
        } catch (Throwable e) {
            LOGGER.info("The game server cannot be started.");
        }
    }

    @OnOpen
    public void addClient(final Session session) {
        if (CLIENTS.size() < ServerConstants.MAX_NUM_OF_CLIENTS) {
            CLIENTS.put(session, null);
            clientSession = session;
            LOGGER.info("The player has successfully joined");
            if (CLIENTS.size() == ServerConstants.MAX_NUM_OF_CLIENTS) {
                LOGGER.info("It has enough players joined. The game initialization is started.");
                nameInvite();
            }
        } else {
            try {
                session.close();
                LOGGER.info("It has enough players joined");
            } catch (IOException e) {
                LOGGER.error("Failed to close the session");
            }
        }
    }

    @OnClose
    public void removeClient(final Session session) {
        CLIENTS.remove(session);
        LOGGER.debug("The client session %s was removed successfully", session.getId());
    }

    @SuppressWarnings("UnusedParameters")
    @OnError
    public void handleClientError(final Session session, final Throwable error) {
        LOGGER.error("An error occurred on the %s client session", session.getId());
        removeClient(session);
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message, final Session session) {
//        if (finishFlag) {
//            System.exit(0);
//        }
        final Object[] args = message.getArgs();
        switch (message.getCommand()) {
            case Commands.WEIGHT:
                weightResponse(args);
                break;
            case Commands.NAME:
                nameResponse(session, args);
                break;
            case Commands.NEXT_VERTICES:
                nextVertices(args);
                break;
            case Commands.MOVE:
//                if (finishFlag) {
//                    System.exit(0);
//                }
//
//                if (nextVertexName.equals(GAME.finishVertex())) {
////                    LOGGER.info("The GAME finished. Player %s won!", message.getParticipant());
//                    finishFlag = true;
//                    System.exit(0);
//                } else {
////                    GAME.move(message.getParticipant(), nextVertexName);
//                    sendMessage(Commands.MOVE, Response.OK);
//                }
                break;
            default:
//                LOGGER.error("The %s command: %s", message.getParticipant(), Commands.UNRECOGNIZABLE);
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
        sendMessage(new Messenger.Message(command, args));
    }

    private void weightResponse(final Object[] args) {
        final String vrtx1 = (String) args[0];
        final String vrtx2 = (String) args[1];
        sendMessage(Commands.WEIGHT, GAME.weight(vrtx1, vrtx2));
    }

    private void nameResponse(final Session session, final Object[] args) {
        final String name = (String) args[0];
        if (GAME.name(session, name)) {
            sendMessage(Commands.NAME, Response.OK);
        } else {
            sendMessage(Commands.NAME, Response.FAIL, name);
        }
    }

    private void moveResponse(final Object[] args) {
        final String nextVertexName = (String) args[0];
    }

    private void nameInvite() {
        sendMessage(Commands.NAME, Response.INVITE);
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    private void nextVertices(final Object[] args) {
        final String currentVertex = (String) args[0];
        final Set<String> nextVerticesSet = GAME.nextVertices(currentVertex);
        String[] nextVertices = new String[nextVerticesSet.size()];
        nextVerticesSet.toArray(nextVertices);
        sendMessage(Commands.NEXT_VERTICES, nextVertices);
    }

    private static class Game {

        private final Field field = new Field();
        private final String finishVertex = startVertices()[1];

        boolean addPlayer(final String playerName) {
            return Player.addPlayer(playerName);
        }

        String[] startVertices() {
            return field.getStartVertices();
        }

        private int weight(final String vrtx1, final String vrtx2) {
            return field.getGameModel().getWeight(vrtx1, vrtx2);
        }

        private boolean name(final Session session, final String name) {
            if (!CLIENTS.values().contains(name)) {
                CLIENTS.put(session, name);
                return true;
            } else {
                return false;
            }
        }

        public String whereIsCompetitor(String id) {
            final Player player = Player.getPlayer(id);
            return player.getCurrentPosition();
        }

        public Set<String> nextVertices(final String vertex) {
            return field.getNextVertices(vertex);
        }

        public boolean move(final String playerName, final String vertexName) {
            final Player player = Player.getPlayer(playerName);
            player.setCurrentPosition(vertexName);
            LOGGER.info("Now player %s in %s", playerName, vertexName);
            return true;
        }

        String finishVertex() {
            return finishVertex;
        }
    }

}
