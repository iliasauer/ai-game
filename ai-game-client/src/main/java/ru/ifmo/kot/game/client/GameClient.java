package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.ai.Ai;
import ru.ifmo.kot.game.ai.AiImpl;
import ru.ifmo.kot.tools.Commands;
import ru.ifmo.kot.tools.Messenger;
import ru.ifmo.kot.tools.Response;

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
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
        final String command = message.getCommand();
        switch (command) {
            case Commands.WEIGHT:
                this.response = message.getArgs()[0];
                break;
            case Commands.NAME:
                this.response = message.getArgs()[0];
                if (response.equals(Response.FAIL)) {
//                    game.nameMe();
                }
                break;
            case Commands.START_DATA:
                final String startVertex = (String) message.getArgs()[0];
                final String finishVertex = (String) message.getArgs()[1];
                game.initStartVertices(startVertex, finishVertex);
                game.knowNextVertices(game.currentVertex());
                break;
            case Commands.NEXT_VERTICES:
                final List<String> nextVertices = new ArrayList<>();
                for (final Object vertex: message.getArgs()) {
                    nextVertices.add((String) vertex);
                }
//                game.move(nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size())));
                break;
            case Commands.MOVE:
                this.response = message.getArgs()[0];
//                if (response.equals(Response.OK)) {
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

    private Future<Object> sendMessage(final String command, final Object... args) {
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

        void nameMe(final String myName) {
            sendMessage(Commands.NAME, myName);
//            GameClient.this.name = myName;
            LOGGER.info("I want my name was %s", myName);
        }

        void knowNextVertices() {
            sendMessage(Commands.NEXT_VERTICES, startVertex);
        }

        void knowNextVertices(final String vertexName) {
            sendMessage(Commands.NEXT_VERTICES, vertexName);
        }

        String currentVertex() {
            return currentVertex;
        }

        void move() {
            sendMessage(Commands.MOVE, ai.move());
        }

        @Override
        public void run() {
//            nameMe(name);
        }

    }

}
