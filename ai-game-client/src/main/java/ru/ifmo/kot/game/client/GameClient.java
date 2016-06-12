package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.aibase.Ai;
import ru.ifmo.kot.game.ai.AiImpl;
import ru.ifmo.kot.game.api.ServerApiImpl;
import ru.ifmo.kot.protocol.function.Action;
import ru.ifmo.kot.protocol.Command;
import ru.ifmo.kot.protocol.Messenger;
import ru.ifmo.kot.protocol.RequestStatus;
import ru.ifmo.kot.protocol.ResponseStatus;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private Session serverSession;
    private Map<String, Object> responseMap = new LinkedHashMap<>();
    private Game game = new Game();

    public static void main(String[] args)
            throws InterruptedException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.MINUTES.toMillis(2));
        try {
            webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL)
            );
            TimeUnit.SECONDS.sleep(30); // todo temp decision
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }
    }

    @OnOpen
    public void addServerSession(final Session session) {
        this.serverSession = session;
        try {
            LOGGER.info("I have joined the game");
        } catch (final Exception e) {
            LOGGER.error("Failed to greet the server");
        }
    }

    @OnClose
    public void handleClose(final Session session) {
        LOGGER.debug("The connection was closed");
    }

    @OnMessage
    public void handleMessage(final Messenger.Message message) {
        final Command command = message.getCommand();
        switch (command) {
            case NAME:
                handleAiCommand(
                        message,
                        game::nameMe,
                        game::nameMeAgain
                );
                break;
            case START_DATA:
                handleServiceCommand(
                        () -> {
                            LOGGER.info("Got start data invite from server");
                            final String startVertex = (String) message.getArgs()[0];
                            final String finishVertex = (String) message.getArgs()[1];
                            game.initStartVertices(startVertex, finishVertex);
                        }
                );
                break;
            case MOVE:
                handleAiCommand(
                        message,
                        game::move,
                        game::moveAgain
                );
                break;
            case CURRENT_VERTEX:
            case NEXT_VERTICES:
            case WEIGHT:
            case COMPETITORS_POSITIONS:
                handleApiCommand(message);
                break;
            case WIN:
                handleEndCommand("I won! Hurrah!");
                break;
            case LOSE:
                handleEndCommand("I lost.");
                break;
            default:

        }
    }

    private void handleEndCommand(final String finalMessageString, Object... params) {
        LOGGER.info(finalMessageString, params);
        try {
            serverSession.close();
        } catch(final IOException e) {
            LOGGER.error("Internal error");
        }
        System.exit(0);
    }

    private void handleApiCommand(final Messenger.Message message) {
        final Optional<ResponseStatus> optRespStatus = message.getResponseStatus();
        if (optRespStatus.isPresent()) {
            final Object answer = message.getArgs()[0];
            handleApiResponse(message.getCommand().name(), optRespStatus.get(), answer);
        }
    }

    private void handleAiCommand(final Messenger.Message message, final Action aiFirstAttemptAction,
                                 final Consumer<String> aiSecondAttemptAction) {
        final Optional<RequestStatus> optReqStatus = message.getRequestStatus();
        final Optional<ResponseStatus> optRespStatus = message.getResponseStatus();
        final String commandName = message.getCommand().name();
        if (optReqStatus.isPresent()) {
            handleRequest(optReqStatus.get(), () -> {
                LOGGER.info("Got %s invite from server", commandName);
                aiFirstAttemptAction.execute();
            });
            return;
        }
        if (optRespStatus.isPresent()) {
            handleResponse(optRespStatus.get(),
                    () -> LOGGER.info("The %s is accepted. Let's continue", commandName),
                    () -> {
                        LOGGER.info("The %s is not accepted. Try again", commandName);
                        final String failCommandParam = (String) message.getArgs()[0];
                        aiSecondAttemptAction.accept(failCommandParam);
                    },
                    () -> LOGGER.info("The %s is not accepted. Wait for the next move", commandName));
        }
    }

    private void handleApiResponse(final String commandName,
                                   final ResponseStatus responseStatus, final Object answer) {
        handleResponse(responseStatus,
                () -> {
//                    LOGGER.info("The %s request is successful", commandName);
                },
                () -> {
//                    LOGGER.info("The %s request is failed", commandName);
                });
        responseMap.put(commandName, answer);
    }


    private void handleServiceCommand(final Action defaultAction) {
        handleRequest(defaultAction);
    }

    private void handleRequest(final Action defaultAction) {
        defaultAction.execute();
    }

    private void handleRequest(final RequestStatus requestStatus,
                               final Action inviteActon) {
        switch (requestStatus) {
            case INVITE:
                inviteActon.execute();
                break;
        }
    }

    private void handleResponse(final ResponseStatus responseStatus,
                                final Action onOkAction, final Action onNotAcceptedAction,
                                final Action onFailAction) {
        switch (responseStatus) {
            case OK:
                onOkAction.execute();
                break;
            case NOT_ACCEPTED:
                onNotAcceptedAction.execute();
                break;
            case FAIL:
                onFailAction.execute();
                break;
        }
    }

    private void handleResponse(final ResponseStatus responseStatus,
                                final Action onOkAction,
                                final Action onFailAction) {
        switch (responseStatus) {
            case OK:
                onOkAction.execute();
                break;
            case FAIL:
                onFailAction.execute();
                break;
        }
    }

    @OnError
    public void handleError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred", error);
    }

    private SendApiMessageTask<?> getSendMessageTask(
            final Command command, final Object... args
    ) {
        return new SendApiMessageTask<>(responseMap, session -> sendMessage(command, args),
            serverSession, command.name());
    }

    @SuppressWarnings("unchecked")
    private void sendMessage(final Messenger.Message message) {
        if (serverSession.isOpen()) {
            serverSession.getAsyncRemote().sendObject(message);
        } else {
            LOGGER.error("Failed to send the message to the server");
        }
    }

    private void sendMessage(final Command command, final Object... args) {
        sendMessage(new Messenger.Message(command, args));
    }

    public class Game {

        private final ExecutorService executor = Executors.newSingleThreadExecutor();
        private final Ai ai = new AiImpl(new ServerApiImpl(GameClient.Game.this));
        private String startVertex;
        private String currentVertex;
        private String finishVertex;

        void initStartVertices(final String startVertex, final String finishVertex) {
            this.startVertex = startVertex;
            this.currentVertex = startVertex;
            this.finishVertex = finishVertex;
            LOGGER.info("I should go from %s to %s", startVertex, finishVertex);
        }

        void nameMe() {
            final String name = ai.name();
            sendMessage(Command.NAME, name);
            LOGGER.info("I want my name was %s", name);
        }

        void nameMeAgain(final String oldName) {
            final String newName = ai.name(oldName);
            sendMessage(Command.NAME, newName);
            LOGGER.info("Then I want my name was %s", newName);
        }

        void move() {
            Executors.newSingleThreadExecutor().submit(() -> {
//                try {
//                    TimeUnit.SECONDS.sleep(1);
//                } catch (InterruptedException e) {
//                    LOGGER.error("The internal error");
//                }
                final String s = ai.move();
                sendMessage(Command.MOVE, s);
            });
        }

        void moveAgain(final String oldMove) {
            Executors.newSingleThreadExecutor().submit(() -> {
                final String s = ai.move();
                sendMessage(Command.MOVE, s);
            });
        }

        @SuppressWarnings("unchecked")
        public List<String> knowNextVertices(final String vertexName) {
            final Future<Void> future =
                    executor.submit(getSendMessageTask(Command.NEXT_VERTICES, vertexName));
//            LOGGER.info("Send NEXT_VERTICES request");
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Internal error");
            } catch(TimeoutException e) {
                LOGGER.error("Timeout error");
            }
            return (List<String>) responseMap.get(Command.NEXT_VERTICES.name());
        }

        public int knowWeight(final String vertexName1, final String vertexName2) {
            final Future<Void> future =
                    executor.submit(getSendMessageTask(Command.WEIGHT, vertexName1, vertexName2));
//            LOGGER.info("Send WEIGHT request"); todo temp
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Internal server error");
            } catch(TimeoutException e) {
                LOGGER.error("Timeout error");
            }
            return (Integer) responseMap.get(Command.WEIGHT.name());
        }

        public String currentVertex() {
            final Future<Void> future =
                    executor.submit(getSendMessageTask(Command.CURRENT_VERTEX));
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Internal server error");
            } catch(TimeoutException e) {
                LOGGER.error("Timeout error");
            }
            return (String) responseMap.get(Command.CURRENT_VERTEX.name());
        }

        public String startVertex() {
            return startVertex;
        }

        public String finishVertex() {
            return finishVertex;
        }

    }
}
