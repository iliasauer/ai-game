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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.MAX_NUM_OF_CLIENTS;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(
        value = "/game",
        encoders = {Messenger.MessageEncoder.class},
        decoders = {Messenger.MessageDecoder.class})
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private static final Game game = new Game();
    private static volatile boolean finishFlag = false;
    private static volatile int added = 0;
    private static volatile int turnCounter = 1;
    private static volatile boolean[] playerTurnFlags = new boolean[MAX_NUM_OF_CLIENTS];
    static {
        resetTurn();
    }
    private static final List<Session> clients = new ArrayList<>(MAX_NUM_OF_CLIENTS);

    private Session clientSession;
    private int localTurnCounter = 0;
    private Set<Integer> turns = new HashSet<>();

    private static boolean checkTurn(final int index, final int localTurnCounter) {
        boolean check = localTurnCounter < turnCounter;
//        boolean check = playerTurnFlags[0];
        for (int i = 1; i < playerTurnFlags.length; i++) {
            if (i < index) {
                check = check && playerTurnFlags[i];
            } else {
                check = check && !playerTurnFlags[i];
            }
        }
        checkAndResetTurn();
        return check;
    }

    private static void checkAndResetTurn() {
        boolean check = true;
        for (final boolean flag: playerTurnFlags) {
            check = check && flag;
        }
        if (check) {
            resetTurn();
            turnCounter++;
        }
    }

    private static void resetTurn() {
        for (int i = 0; i < playerTurnFlags.length; i++) {
            playerTurnFlags[i] = false;
        }
    }

    private static int checkIn(final Session session) {
        final int index = clients.indexOf(session);
        playerTurnFlags[index] = true;
        return index;
    }

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
            added++;
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
        if (finishFlag) {
            System.exit(0);
        }
        final Object[] args = message.getArgs();
        switch (message.getCommand()) {
            case Commands.WEIGHT:
                final String vrtx1 = (String) args[0];
                final String vrtx2 = (String) args[1];
                final int weight = game.weight(vrtx1, vrtx2);
                sendMessage( Commands.WEIGHT, weight);
                break;
            case Commands.NAME:
                final String playerName = (String) args[0];
                if (game.addPlayer(playerName)) {
                    LOGGER.info("The player was added successfully as %s", playerName);
                    sendMessage(Commands.NAME, Response.OK);
                    while (clients.size() < MAX_NUM_OF_CLIENTS) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(100);
                        } catch (InterruptedException ignored) {
                        }
                    }
                    sendMessage(Commands.START_DATA, game.startVertices());
                } else {
                    LOGGER.info("Failed to add the player");
                    sendMessage(Commands.NAME, Response.FAIL);
                }
                break;
            case Commands.NEXT_VERTICES:
                final String currentVertexName = (String) args[0];
                final Set<String> nextVerticesSet = game.nextVertices(currentVertexName);
                String[] nextVertices = new String[nextVerticesSet.size()];
                nextVerticesSet.toArray(nextVertices);
                sendMessage(Commands.NEXT_VERTICES, nextVertices);
                break;
            case Commands.MOVE:
                final int i = checkIn(session);
                while (checkTurn(i, localTurnCounter) && added < MAX_NUM_OF_CLIENTS) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (finishFlag) {
                    System.exit(0);
                }
                final String nextVertexName = (String) args[0];
                if (nextVertexName.equals(game.finishVertex())) {
                    LOGGER.info("The game finished. Player %s won!", message.getParticipant());
                    finishFlag = true;
                    System.exit(0);
                } else {
                    game.move(message.getParticipant(), nextVertexName);
                    sendMessage(Commands.MOVE, Response.OK);
                }
                localTurnCounter++;
                turns.add(turnCounter);
                break;
            default:
                LOGGER.error("The %s command: %s", message.getParticipant(),
                        Commands.UNRECOGNIZABLE);
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

    private static class Game implements Api {

        private final Field field = new Field();
        private final String finishVertex = startVertices()[1];

        boolean addPlayer(final String playerName) {
            return Player.addPlayer(playerName);
        }

        String[] startVertices() {
            return field.getStartVertices();
        }

        @Override
        public int weight(final String vertex1, final String vertex2) {
            return field.getGameModel().getWeight(vertex1, vertex2);
        }

        @Override
        public String whereIsCompetitor(String id) {
            final Player player = Player.getPlayer(id);
            return player.getCurrentPosition();
        }

        @Override
        public Set<String> nextVertices(final String vertex) {
            return field.getNextVertices(vertex);
        }

        @Override
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
