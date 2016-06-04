package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.aibase.Ai;
import ru.ifmo.kot.game.ai.AiImpl;
import ru.ifmo.kot.game.api.ServerApiImpl;
import ru.ifmo.kot.tools.Command;
import ru.ifmo.kot.tools.Messenger;
import ru.ifmo.kot.tools.ResponseStatus;

import javax.websocket.ClientEndpoint;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.EncodeException;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private static final Random USUAL_RANDOM = new Random();
    private static final Map<Session, String> SERVER_SESSIONS = new HashMap<>(2);
    private Session serverSession;
    private Object response;
    private Game game = new Game();

    public static void main(String[] args)
            throws InterruptedException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.MINUTES.toMillis(2));
        try {
            webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL)
            ); // todo check is it the same serverSession?
            TimeUnit.SECONDS.sleep(30);
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }
    }

    public Game game() {
        return game;
    }

    @OnOpen
    public void addServerSession(final Session session) {
        this.serverSession = session;
        try {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(game);
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
    public void handleMessage(final Messenger.Message message, final Session session) {
        final Command command = message.getCommand();
        final Optional<ResponseStatus> optionalResponseStatus = message.getResponseStatus();
        final ResponseStatus responseStatus;
        switch (command) {
            case WEIGHT:
                response = message.getArgs()[0];
                break;
            case NAME:
                if (optionalResponseStatus.isPresent()) {
                    responseStatus = optionalResponseStatus.get();
                } else {
                    LOGGER.error("The wrong server message format");
                    throw new IllegalArgumentException();
                }
                switch (responseStatus) {
                    case INVITE:
                        game.nameMe();
                        break;
                    case OK:
                        LOGGER.info("Name is accepted. Let's play");
                        break;
                    case FAIL:
                        LOGGER.info("Name is not accepted. That's a crap");
                        final String oldName = (String) message.getArgs()[0];
                        game.nameMeAgain(oldName);
                }
                break;
            case START_DATA:
                final String startVertex = (String) message.getArgs()[0];
                final String finishVertex = (String) message.getArgs()[1];
                game.initStartVertices(startVertex, finishVertex);
                break;
            case NEXT_VERTICES:
                if (optionalResponseStatus.isPresent()) {
                    responseStatus = optionalResponseStatus.get();
                } else {
                    LOGGER.error("The wrong server message format");
                    throw new IllegalArgumentException();
                }
                switch (responseStatus) {
                    case OK:
                        final List<String> nextVertices = new ArrayList<>();
                        for (final Object vertex : message.getArgs()) {
                            nextVertices.add((String) vertex);
                        }
                        response = nextVertices;
                        break;
                }
                break;
            case MOVE:
                if (optionalResponseStatus.isPresent()) {
                    responseStatus = optionalResponseStatus.get();
                } else {
                    LOGGER.error("The wrong server message format");
                    throw new IllegalArgumentException();
                }
                switch (responseStatus) {
                    case INVITE:
                        game.move();
                        break;
                    case OK:
                        LOGGER.info("I think it was a good move");
                        break;
                    case FAIL:
                        LOGGER.info("Move is not accepted. That's a crap");
                        final String move = (String) message.getArgs()[0];
                        game.moveAgain(move);
                }
                break;
            default:
//                LOGGER.info("The %s command response:", message.getParticipant(), Commands.UNRECOGNIZABLE);
        }
    }

    @OnError
    public void handleError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred");
    }

    @SuppressWarnings("unchecked")
    private <T> T sendMessage(final boolean waitResponse, final Messenger.Message message) {
        if (serverSession.isOpen()) {
            try {
                serverSession.getBasicRemote().sendObject(message);
            } catch (final IOException | EncodeException e) {
                LOGGER.error("Failed to send message to the server");
            }
            if (waitResponse) {
                final ExecutorService executor = Executors.newSingleThreadExecutor();
                final Future<T> future = executor.submit(() -> {
                    while (this.response == null) {
                        TimeUnit.MILLISECONDS.sleep(10);
                    }
                    final T response = (T) this.response;
                    this.response = null;
                    return response;
                });
                try {
                    return future.get();
                } catch (final InterruptedException | ExecutionException e) {
                    LOGGER.error("Failed to get a message from the server");
                }
            }
        } else {
            LOGGER.error("Failed to send the message to the server");
        }
        return null;
    }


    private <T> T sendMessage(final boolean waitResponse, final Command command, final Object ...
            args) {
        return sendMessage(waitResponse, new Messenger.Message(command, args));
    }

    private class SendMessageTask<T> implements Callable<T> {

        private Object response;
        private final Command command;
        private final Object[] args;

        SendMessageTask(Command command, Object ... args) {
            this.command = command;
            this.args = args;
        }

        @Override
        public T call() throws Exception {
            return sendMessage(true, command, args);
        }
    }

    public class Game implements Runnable {

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
            sendMessage(false, Command.NAME, name);
            LOGGER.info("I want my name was %s", name);
        }

        void nameMeAgain(final String oldName) {
            final String newName = ai.name(oldName);
            sendMessage(false, Command.NAME, newName);
            LOGGER.info("Then I want my name was %s", newName);
        }

        @SuppressWarnings("unchecked")
        public List<String> knowNextVertices() {
            return (List<String>) sendMessage(true, Command.NEXT_VERTICES, startVertex);
        }

        @SuppressWarnings("unchecked")
        public List<String> knowNextVertices(final String vertexName) {
            try {
                return (List<String>) executor.submit(new SendMessageTask<>(Command
                        .NEXT_VERTICES, vertexName)).get(5, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException e) {
                LOGGER.info("Can't get the response");
            }
            return null;
        }

        public int knowWeight(final String vertexName1, final String vertexName2) {
            return (Integer) sendMessage(true, Command.WEIGHT, vertexName1, vertexName2);
        }

        public String currentVertex() {
            return currentVertex;
        }

        public String startVertex() {
            return startVertex;
        }

        public String finishVertex() {
            return finishVertex;
        }

        void move() {
            sendMessage(false, Command.MOVE, ai.move());
        }

        void moveAgain(final String oldMove) {

        }

        @Override
        public void run() {
//            nameMe(name);
        }

    }

}
