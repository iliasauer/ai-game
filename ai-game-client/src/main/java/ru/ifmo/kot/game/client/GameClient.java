package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.aibase.Ai;
import ru.ifmo.kot.game.ai.AiImpl;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

    @OnOpen
    public void addServerSession(final Session session) {
        this.serverSession = session;
        try {
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.submit(game);
            LOGGER.info("I have joined the game");
        } catch (Exception e) {
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
        if (optionalResponseStatus.isPresent()) {
            responseStatus = optionalResponseStatus.get();
        } else {
            LOGGER.error("The wrong server message format");
            throw new IllegalArgumentException();
        }
        switch (command) {
            case WEIGHT:
                break;
            case NAME:
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
                game.knowNextVertices(game.currentVertex());
                break;
            case NEXT_VERTICES:
                final List<String> nextVertices = new ArrayList<>();
                for (final Object vertex: message.getArgs()) {
                    nextVertices.add((String) vertex);
                }
//                game.move(nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size())));
                break;
            case MOVE:
                this.response = message.getArgs()[0];
//                if (response.equals(ResponseStatus.OK)) {
//                    LOGGER.info("Ok. Now I should do a next move");
//                }
                game.knowNextVertices(game.currentVertex());
                break;
            default:
//                LOGGER.info("The %s command response:", message.getParticipant(), Commands.UNRECOGNIZABLE);
        }
    }

    @OnError
    public void handleError(final Session session, final Throwable error) {
        LOGGER.debug("An error occurred");
    }

    private Future<Object> sendMessage(final Messenger.Message message) {
        if (serverSession.isOpen()) {
            try {
                serverSession.getBasicRemote().sendObject(message);
            } catch (IOException | EncodeException e) {
                LOGGER.error("Failed to send message to the server");
            }
            final ExecutorService executor = Executors.newSingleThreadExecutor();
            return executor.submit(() -> {
                while (this.response == null) {
                    TimeUnit.MILLISECONDS.sleep(10);
                }
                return this.response;
            });
        } else {
            LOGGER.error("Failed to send message to the server");
        }
        return null;
    }

    private Future<Object> sendMessage(final Command command, final Object... args) {
        return sendMessage(new Messenger.Message(command, args));
    }

    private class Game implements Runnable {

        private final Ai ai = new AiImpl();
        private String startVertex;
        private String currentVertex;

        void initStartVertices(final String startVertex, final String finishVertex) {
            this.startVertex = startVertex;
            this.currentVertex = startVertex;
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

        void knowNextVertices() {
            sendMessage(Command.NEXT_VERTICES, startVertex);
        }

        void knowNextVertices(final String vertexName) {
            sendMessage(Command.NEXT_VERTICES, vertexName);
        }

        String currentVertex() {
            return currentVertex;
        }

        void move() {
            sendMessage(Command.MOVE, ai.move());
        }

        @Override
        public void run() {
//            nameMe(name);
        }

    }

}
