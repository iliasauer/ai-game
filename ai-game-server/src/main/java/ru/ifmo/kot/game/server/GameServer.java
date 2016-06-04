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
import ru.ifmo.kot.tools.Command;
import ru.ifmo.kot.tools.EmbeddedLogger;
import ru.ifmo.kot.tools.Messenger;
import ru.ifmo.kot.tools.ResponseStatus;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.PORT;

@ServerEndpoint(
        value = "/game",
        encoders = {Messenger.MessageEncoder.class},
        decoders = {Messenger.MessageDecoder.class})
public class GameServer {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameServer.class);
    private static final Game GAME = new Game();
    private static final ExecutorService INVITE_EXECUTOR = Executors.newSingleThreadExecutor();
    private static final List<Session> clients = new ArrayList<>();
    private static Map<String, Boolean> turnMap = new LinkedHashMap<>();
    private Session localClient;

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
    public void addClient(final Session client) {
        this.localClient = client;
        if (clients.size() < ServerConstants.MAX_NUM_OF_CLIENTS) {
            clients.add(client);
            final String clientId = client.getId();
            turnMap.put(clientId, Boolean.FALSE);
            LOGGER.info("The player has successfully joined");
            if (clients.size() == ServerConstants.MAX_NUM_OF_CLIENTS) {
                LOGGER.info("It has enough players joined. The game initialization is started.");
                nameInvite();
            }
        } else {
            try {
                client.close();
                LOGGER.info("It has enough players joined");
            } catch (final IOException e) {
                LOGGER.error("Failed to close the session");
            }
        }
    }

    @OnClose
    public void removeClient(final Session session) {
//        CLIENTS_MAP.remove(session);
        LOGGER.debug("The client session %s was removed successfully", session.getId());
    }

    @SuppressWarnings("UnusedParameters")
    @OnError
    public void handleClientError(final Session session, final Throwable error) {
        LOGGER.error("An error occurred on the %s client session", session.getId());
        LOGGER.error("", error);
        removeClient(session);
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message, final Session session) {
//        if (finishFlag) {
//            System.exit(0);
//        }
        final Object[] args = message.getArgs();
        switch (message.getCommand()) {
            case WEIGHT:
                weightResponse(args);
                break;
            case NAME:
                nameResponse(session, args);
                break;
            case NEXT_VERTICES:
                nextVertices(args);
                break;
            case MOVE:
                moveResponse(session, args);
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
//                    sendMessage(Commands.MOVE, ResponseStatus.OK);
//                }
                break;
            default:
//                LOGGER.error("The %s command: %s", message.getParticipant(), Commands.UNRECOGNIZABLE);
        }
    }

    private static void sendMessage(final Session session, final Messenger.Message message) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendObject(message);
            } catch (final IOException | EncodeException e) {
                LOGGER.error("Failed to send a message to the client");
            }
        }
    }

    private void sendMessage(final Messenger.Message message) {
        if (localClient.isOpen()) { // todo check the need
            try {
                localClient.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                LOGGER.error("Failed to send a message to the client");
            }
        } else {
            removeClient(localClient);
        }
    }

    private static void sendMessage(final Session session,
                                    final Command command,
                                    final ResponseStatus status,
                                    final Object... args) {
        sendMessage(session, new Messenger.Message(command, status, args));
    }

    private static void sendMessage(final Session session,
                                    final Command command,
                                    final Object... args) {
        sendMessage(session, new Messenger.Message(command, args));
    }

    private void sendMessage(final Command command, final ResponseStatus status,
                             final Object ... args) {
        sendMessage(new Messenger.Message(command, status, args));
    }

    private void weightResponse(final Object[] args) {
        final String vrtx1 = (String) args[0];
        final String vrtx2 = (String) args[1];
        sendMessage(Command.WEIGHT, ResponseStatus.OK, GAME.weight(vrtx1, vrtx2));
    }

    private void nameInvite() {
        INVITE_EXECUTOR.submit(SendInviteTask.nameInviteTask());
    }

    private void moveInvite() {
        INVITE_EXECUTOR.submit(SendInviteTask.moveInviteTask());
    }

    private void nameResponse(final Session client, final Object[] args) {
        final String name = (String) args[0];
        if (GAME.name(client, name)) {
            sendMessage(client, Command.NAME, ResponseStatus.OK);
            turnMap.put(client.getId(), Boolean.TRUE);
            sendMessage(client, Command.START_DATA, GAME.startVertex(), GAME.finishVertex());
        } else {
            sendMessage(client, Command.NAME, ResponseStatus.FAIL, name);
            turnMap.put(client.getId(), Boolean.FALSE);
        }
        boolean commonReady = true;
        for (boolean ready: turnMap.values()) {
            commonReady = commonReady && ready;
        }
        if (commonReady) {
            moveInvite();
        }
    }

    private void moveResponse(final Session client, final Object[] args) {
        final String nextVertexName = (String) args[0];
        final Player player = GAME.players().get(client.getId());
        player.setCurrentPosition(nextVertexName);
        LOGGER.info("Now %s in %s", player.getName(), player.getCurrentPosition());
        sendMessage(client, Command.NAME, ResponseStatus.OK);
        turnMap.put(client.getId(), Boolean.TRUE);
    }

    private static class SendInviteTask implements Runnable {

        private static final ExecutorService WAITING_TASK_EXECUTOR = Executors.newSingleThreadExecutor();
        private static final SendInviteTask NAME_INVITE_TASK = new SendInviteTask(Command.NAME);
        private static final SendInviteTask MOVE_INVITE_TASK = new SendInviteTask(Command.MOVE);
        private static Map<String, WaitingClientTask> waitingTasks;

        private final Command command;

        SendInviteTask(final Command command) {
            if (waitingTasks == null) {
                init();
            }
            this.command = command;
        }

        static void init() {
            waitingTasks = clients.stream().collect(Collectors.toMap(
                    Session::getId,
                    session -> new WaitingClientTask(session.getId()),
                    (task1, task2) -> null));
        }

        @Override
        public void run() {
            IntStream.range(0, clients.size()).forEach(i -> {
                final Session client = clients.get(i);
                final String clientId = client.getId();
                LOGGER.info("Send %s invite to player #%d", command, (i + 1));
                if (turnMap.get(clientId).equals(Boolean.FALSE)) {
                    sendMessage(client, command, ResponseStatus.INVITE);
                    final Future<Boolean> future = WAITING_TASK_EXECUTOR.submit(new
                            WaitingClientTask(clientId));
                    try {
                        future.get(5, TimeUnit.SECONDS);
                    } catch (final InterruptedException | ExecutionException e) {
                        LOGGER.error("Internal server error");
                    } catch (TimeoutException e) {
                        LOGGER.error("Client does not respond");
                    }
                }
            });
            turnMap.replaceAll((k, v) -> Boolean.FALSE);
        }

        static SendInviteTask nameInviteTask() {
            return NAME_INVITE_TASK;
        }

        static SendInviteTask moveInviteTask() {
            return MOVE_INVITE_TASK;
        }

        private static class WaitingClientTask implements Callable<Boolean> {

            private final String clientId;

            WaitingClientTask(final String clientId) {
                this.clientId = clientId;
            }

            @Override
            public Boolean call() throws Exception {
                while (turnMap.get(clientId).equals(Boolean.FALSE)) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1L);
                    } catch (InterruptedException e) {
                        throw new IllegalStateException(e);
                    }
                }
                return Boolean.TRUE;
            }
        }
    }

    @SuppressWarnings("ConfusingArgumentToVarargsMethod")
    private void nextVertices(final Object[] args) {
        final String currentVertex = (String) args[0];
        final Set<String> nextVerticesSet = GAME.nextVertices(currentVertex);
        String[] nextVertices = new String[nextVerticesSet.size()];
        nextVerticesSet.toArray(nextVertices);
        sendMessage(Command.NEXT_VERTICES, ResponseStatus.OK, nextVertices);
    }

    private static class Game {

        private Map<String, Player> players = new LinkedHashMap<>();
        private Set<String> names = new HashSet<>();
        private final Field field = new Field();
        private final String startVertex = startVertices()[0];
        private final String finishVertex = startVertices()[1];

//        boolean addPlayer(final String playerName) {
//            return Player.addPlayer(playerName);
//        }

        String[] startVertices() {
            return field.getStartVertices();
        }

        private int weight(final String vrtx1, final String vrtx2) {
            return field.getGameModel().getWeight(vrtx1, vrtx2);
        }

        private boolean name(final Session session, final String name) {
            if (!names.contains(name)) {
                names.add(name);
                players.put(session.getId(), new Player(name, startVertex));
                LOGGER.info("There is %s name for session %s", name, session.getId());
                return true;
            } else {
                LOGGER.info("The %s name is already occupied", name);
                return false;
            }
        }

//        public String whereIsCompetitor(String id) {
//            final Player player = Player.getPlayer(id);
//            return player.getCurrentPosition();
//        }

        public Set<String> nextVertices(final String vertex) {
            return field.getNextVertices(vertex);
        }

//        public boolean move(final String playerName, final String vertexName) {
//            final Player player = Player.getPlayer(playerName);
//            player.setCurrentPosition(vertexName);
//            LOGGER.info("Now player %s in %s", playerName, vertexName);
//            return true;
//        }


        String startVertex() {
            return startVertex;
        }

        String finishVertex() {
            return finishVertex;
        }

        public Map<String, Player> players() {
            return players;
        }
    }

}
