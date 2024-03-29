package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import ru.ifmo.kot.game.elements.Field;
import ru.ifmo.kot.game.elements.Player;
import ru.ifmo.kot.game.model.EdgeContent;
import ru.ifmo.kot.game.model.SymbolGraph;
import ru.ifmo.kot.game.visualiztion.Event;
import ru.ifmo.kot.game.visualiztion.ViewMessage;
import ru.ifmo.kot.game.visualiztion.VisualizationEndpoint;
import ru.ifmo.kot.protocol.Command;
import ru.ifmo.kot.protocol.Messenger;
import ru.ifmo.kot.protocol.RequestStatus;
import ru.ifmo.kot.protocol.ResponseStatus;
import ru.ifmo.kot.protocol.function.Action;
import ru.ifmo.kot.util.EmbeddedLogger;

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
import java.util.ConcurrentModificationException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        } catch (final Throwable e) {
            LOGGER.info("The game server cannot be started.");
        }
    }

    public static Game game() {
        return GAME;
    }

    @OnOpen
    public void addClient(final Session client) {
        this.localClient = client;
        if (clients.size() < ServerConstants.NUM_OF_CLIENTS) {
            clients.add(client);
            LOGGER.info("The player has successfully joined");
            if (clients.size() == ServerConstants.NUM_OF_CLIENTS) {
                LOGGER.info("It has enough players joined. The game initialization is started.");
                GAME.setTurnFuture(nameInvite());
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
    public void removeClient(final Session client) {
        clients.remove(client);
        LOGGER.debug("The player session %s was removed successfully", GAME.getClientName(client));
    }

    @SuppressWarnings("UnusedParameters")
    @OnError
    public void handleClientError(final Session client, final Throwable error) {
        LOGGER.error("An error occurred with %s", GAME.getClientName(client));
        LOGGER.error(error);
        removeClient(client);
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message, final Session client) {
        final Object[] args = message.getArgs();
        final Command command = message.getCommand();
        switch (command) {
            case NAME:
                handleAiCommand(command, client, (String) args[0], GAME::name,
                        () -> sendStartData(client)
                );
                break;
            case MOVE:
                handleAiCommand(command, client, (String) args[0], GAME::move, () -> {
                });
                break;
            case CURRENT_VERTEX:
                handleApiCommand(command, args, (params) -> GAME.currentVertex(client.getId()));
                break;
            case NEXT_VERTICES:
                handleApiCommand(command, args, (params) -> {
                    final String vrtx = (String) params[0];
                    return GAME.nextVertices(vrtx);
                });
                break;
            case WEIGHT:
                handleApiCommand(command, args, (params) -> {
                    final String vrtx1 = (String) params[0];
                    final String vrtx2 = (String) params[1];
                    return GAME.weight(vrtx1, vrtx2);
                });
                break;
            case COMPETITORS_POSITIONS:
                handleApiCommand(command, args, (params) -> GAME.competitorsPositions());
                break;
            default:
        }
    }

    private void handleAiCommand(
            final Command command, final Session client, final String arg,
            final BiPredicate<Session, String> commandAction, final Action onOkAction
    ) {
        final String clientId = client.getId();
        final String clientName = GAME.getClientName(client);
        final String commandName = command.name();
        final ConcurrentMap<String, ResponseStatus> turnMap =
                GAME.turnMap();
        final Consumer<Action> commandReaction = (onFalseAction) -> {
            if (commandAction.test(client, arg)) {
                turnMap.put(clientId, ResponseStatus.OK);
                LOGGER.info("%s of %s is accepted", commandName, clientName);
                sendOkMessage(client, command);
                GAME.checkWinEvent();
                onOkAction.execute();
            } else {
                onFalseAction.execute();
            }
        };
        if (turnMap.containsKey(clientId)) {
            final ResponseStatus status = turnMap.get(clientId);
            switch (status) {
                case NOT_ACCEPTED:
                    commandReaction.accept(() -> {
                        turnMap.put(clientId, ResponseStatus.FAIL);
                        LOGGER.info("%s of %s is failed", commandName, clientName);
                        sendMessage(client, command, ResponseStatus.FAIL, arg);
                    });
                    break;
                default:
                    LOGGER.error("Invalid action");
            }
        } else {
            commandReaction.accept(() -> {
                turnMap.put(clientId, ResponseStatus.NOT_ACCEPTED);
                LOGGER.info("%s of %s is not accepted. Wait for a second attempt", commandName,
                        clientName
                );
                sendMessage(client, command, ResponseStatus.NOT_ACCEPTED, arg);
            });
        }
        GAME.tryNextTurn();
    }

    private <T> void handleApiCommand(
            final Command command, final Object[] args, final Function<Object[], T> apiMethod
    ) {
        handleApiRequest(command, args, apiMethod);
    }

    private <T> void handleApiRequest(
            final Command command, final Object[] args, final Function<Object[], T> apiMethod
    ) {
        final Optional<T> opt = Optional.of(apiMethod.apply(args));
        if (opt.isPresent()) {
            sendMessage(command, ResponseStatus.OK, opt.get());
        } else {
            sendMessage(command, ResponseStatus.FAIL, args);
        }
    }


    private static void sendMessage(final Session session, final Messenger.Message message) {
        if (session.isOpen()) {
            try {
                session.getBasicRemote().sendObject(message);
            } catch (final IOException | EncodeException e) {
                LOGGER.error("Failed to send the message to the client");
            }
        }
    }

    private void sendMessage(final Messenger.Message message) {
        if (localClient.isOpen()) {
            try {
                localClient.getBasicRemote().sendObject(message);
            } catch (final IOException | EncodeException e) {
                LOGGER.error("Failed to send the message to the client");
            }
        } else {
            removeClient(localClient);
        }
    }

    private static void sendMessage(
            final Session session, final Command command, final RequestStatus status,
            final Object... args
    ) {
        sendMessage(session, new Messenger.Message(command, status, args));
    }

    private static void sendMessage(
            final Session session, final Command command, final ResponseStatus status,
            final Object... args
    ) {
        sendMessage(session, new Messenger.Message(command, status, args));
    }

    private static void sendMessage(
            final Session session, final Command command, final Object... args
    ) {
        sendMessage(session, new Messenger.Message(command, args));
    }

    private void sendMessage(
            final Command command, final ResponseStatus status, final Object... args
    ) {
        sendMessage(new Messenger.Message(command, status, args));
    }

    private static SendInvitesTask getSendMessageTask(final Command command) {
        return new SendInvitesTask(GAME.turnMap(),
                client -> {
                    sendMessage(client, command, RequestStatus.INVITE);
                    LOGGER.info("Sent %s invite to %s", command, GAME.getClientName(client));
                },
                clients);
    }

    private static Future<Void> nameInvite() {
        return INVITE_EXECUTOR.submit(getSendMessageTask(Command.NAME));
    }

    private static Future<Void> moveInvite() {
        return INVITE_EXECUTOR.submit(getSendMessageTask(Command.MOVE));
    }

    private void sendStartData(final Session client) {
        sendMessage(client, Command.START_DATA, GAME.startVertex(), GAME.finishVertex());
    }

    private void sendOkMessage(final Session client, final Command command) {
        sendMessage(client, command, ResponseStatus.OK);
    }

    @SuppressWarnings("WeakerAccess")
    public static class Game {
        private ConcurrentMap<String, ResponseStatus> turnMap = new ConcurrentHashMap<>(2);
        private volatile int turnCounter = 0;
        private Future<Void> turnFuture;
        private Map<String, Player> players = new LinkedHashMap<>();
        private final Field field = new Field();
        private final String startVertex = startVertices()[0];
        private final String finishVertex = startVertices()[1];

        public Field field() {
            return field;
        }

        private int getTurnNumber() {
            return turnCounter;
        }

        private void toNextTurn() {
            turnCounter++;
        }

        private void setTurnFuture(final Future<Void> turnFuture) {
            Game.this.turnFuture = turnFuture;
        }

        private void tryNextTurn() {
            if (checkTurnMap()) {
                nextTurn();
            }
        }

        private void nextTurn() {
            toNextTurn();
            LOGGER.info("TURN #%d", getTurnNumber());
            try {
                turnFuture.get(2, TimeUnit.SECONDS);
            } catch(final ConcurrentModificationException | InterruptedException | ExecutionException ignored) {
//              todo fix it
            } catch(final TimeoutException e) {
                LOGGER.error("Turn waiting error");
            }
            turnMap.clear();
            movePlayers();
            if (turnMap.values().stream().filter(status ->
                    status.equals(ResponseStatus.PASS)).count() == clients.size()) {
                LOGGER.debug("BOTH PASS");
                nextTurn();
            } else {
                turnFuture = moveInvite();
            }
        }

        private boolean checkTurnMap() {
            return turnMap.values().stream().filter((status) -> status.equals(ResponseStatus.OK) ||
                status.equals(ResponseStatus.FAIL) || status.equals(ResponseStatus.PASS)).count() >= clients.size();
        }

        private String getClientName(final Session client) {
            final String sessionId = client.getId();
            final Player player = players.get(sessionId);
            if (! Objects.isNull(player)) {
                return player.getName();
            } else {
                return sessionId;
            }
        }

        private ConcurrentMap<String, ResponseStatus> turnMap() {
            return turnMap;
        }

        private boolean isNameOccupied(final String name) {
            return players.values().stream().anyMatch(player -> player.getName().equals(name));
        }

        private boolean name(final Session client, final String name) {
            final String clientId = client.getId();
            if (! isNameOccupied(name)) {
                players.put(clientId, new Player(name, startVertex));
                VisualizationEndpoint.sendMessage(new ViewMessage(name, startVertex));
                LOGGER.info("There is %s name for the client %s", name, clientId);
                return true;
            } else {
                LOGGER.info("The %s name is already occupied", name);
                return false;
            }
        }

        String[] startVertices() {
            return field.getStartVertices();
        }

        String startVertex() {
            return startVertex;
        }

        String finishVertex() {
            return finishVertex;
        }

        private void movePlayers() {
            players.forEach((clientId, player) -> {
                final boolean reached = player.getCloseToExpectedPosition();
                if (! reached) {
                    turnMap.put(clientId, ResponseStatus.PASS);
                }
            });
            checkWinEvent();
        }

        boolean move(final Session client, final String nextVertex) {
            if (field.doesVertexExist(nextVertex)) {
                final String clientId = client.getId();
                final Player player = players.get(clientId);
                final String currentVertex = player.getCurrentPosition();
                if (field.doesEdgeExist(currentVertex, nextVertex)) {
                    final SymbolGraph gameModel = field.getGameModel();
                    final String playerName = player.getName();
                    final Optional<EdgeContent> optEdgeContent =
                            Optional.ofNullable(gameModel.takeEdgeContent(currentVertex, nextVertex));
                    if (optEdgeContent.isPresent()) {
                        final EdgeContent edgeContent = optEdgeContent.get();

                        switch (edgeContent) {
                            case BENEFIT:
                                LOGGER.info("The player %s got a %s", playerName, edgeContent.name());
                                player.enableTempAcceleration();
                                VisualizationEndpoint.sendMessage(
                                        new ViewMessage(playerName, Event.BONUS));
                                break;
                            case OBSTACLE:
                                VisualizationEndpoint.sendMessage(
                                        new ViewMessage(playerName, Event.OBSTACLE));
                                LOGGER.info("The player %s stumbled upon %s", playerName,
                                        edgeContent.name());
                                final boolean isAlive = player.removeLife();
                                if (! isAlive) {
                                    LOGGER.info("The player %s is dead", playerName);
                                    sendMessage(client, Command.LOSE);
                                    VisualizationEndpoint.sendMessage(
                                            new ViewMessage(playerName, Event.LOSE));
                                    clients.remove(client);
                                    if (clients.isEmpty()) {
                                        LOGGER.info("The game over");
                                        System.exit(0);
                                    }
                                    return true;
                                }
                                field.changeWeight(currentVertex, nextVertex);
                                break;
                        }
                    }
                    player.setExpectedPosition(nextVertex, weight(currentVertex, nextVertex));
                    return true;
                } else {
                    LOGGER.info("There is no edge between vertices %s and %s", currentVertex,
                            nextVertex);
                }
            } else {
                LOGGER.info("The vertex %s does not exist", nextVertex);
            }
            return false;
        }

        private void checkWinEvent() {
            clients.forEach(client -> {
                final String clientId = client.getId();
                if (players.containsKey(clientId)) {
                    final Player player = players.get(clientId);
                    if (player.getCurrentPosition().equals(finishVertex)) {
                        LOGGER.info("The player %s won! Congratulations!", player.getName());
                        sendMessage(client, Command.WIN);
                        VisualizationEndpoint.sendMessage(
                                new ViewMessage(getClientName(client), Event.WIN));
                        clients.stream().filter((session) -> ! client.equals(session)).forEach(
                                session -> {
                                    sendMessage(session, Command.LOSE);
                                    VisualizationEndpoint.sendMessage(
                                            new ViewMessage(getClientName(session), Event.LOSE));
                                });
                        System.exit(0);
                    }
                }
            });
        }

        String currentVertex(final String clientId) {
            return players.get(clientId).getCurrentPosition();
        }

        Set<String> nextVertices(final String vertex) {
            if (field.doesVertexExist(vertex)) {
                return field.getNextVertices(vertex);
            } else {
                return null;
            }
        }

        Integer weight(final String vrtx1, final String vrtx2) {
            if (field.doesVertexExist(vrtx1)) {
                if (field.doesVertexExist(vrtx2)) {
                    if (! field.doesEdgeExist(vrtx1, vrtx2)) {
                        LOGGER.info("There is no edge between vertices %s and %s", vrtx1, vrtx2);
                    }
                    return field.getGameModel().getWeight(vrtx1, vrtx2);
                }
            }
            return null;
        }

        Map<String, String> competitorsPositions() {
            return players.values().stream().collect(
                    Collectors.toMap(Player::getName, Player::getCurrentPosition,
                            (position1, position2) -> {
                                LOGGER.error("The player position's collision");
                                return "collision";
                            }
                    ));
        }

    }
}
