package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.tools.ApiCommands;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

    private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
    private static final Random USUAL_RANDOM = new Random();
    private static final String GREETING = "The connection is open";
    private Session serverSession;
    private String name = "player";
    private Object response;
    private Game game = new Game();

    @OnOpen
    public void greetServer(final Session session) {
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
            case ApiCommands.WEIGHT:
                this.response = message.getArgs()[0];
                break;
            case ApiCommands.NAME:
                this.response = message.getArgs()[0];
                if (response.equals(Response.FAIL)) {
                    game.setName();
                }
                break;
            case ApiCommands.START_DATA:
                final String startVertex = (String) message.getArgs()[0];
                final String finishVertex = (String) message.getArgs()[1];
                game.initStartVertices(startVertex, finishVertex);
                game.knowNextVertices();
                break;
            case ApiCommands.NEXT_VERTICES:
                final List<String> nextVertices = new ArrayList<>();
                for (final Object vertex: message.getArgs()) {
                    nextVertices.add((String) vertex);
                }
                game.move(nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size())));
                break;
            case ApiCommands.MOVE:
                this.response = message.getArgs()[0];
                if (response.equals(Response.OK)) {
                    LOGGER.info("Ok. Now I should do a next move");
                }
                break;
            default:
                LOGGER.info("The %s command response:", message.getParticipant(),
                        ApiCommands.UNRECOGNIZABLE
                );
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
        return sendMessage(new Messenger.Message(name, command, args));
    }

    public static void main(String[] args)
            throws InterruptedException {
        final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
        webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.SECONDS.toMillis(30));
        try {
            webSocketContainer.connectToServer(GameClient.class,
                    URI.create(ClientConstants.SERVER_URL)
            ); // todo check is it the same serverSession?
            TimeUnit.SECONDS.sleep(30);
        } catch (DeploymentException | IOException e) {
            LOGGER.error("Failed to connect to the server");
        }
    }

    private class Game implements Runnable {

        private String startVertex;
        private String finishVertex;

        void initStartVertices(final String startVertex, final String finishVertex) {
            this.startVertex = startVertex;
            this.finishVertex = finishVertex;
            LOGGER.info("I should go from %s to %s", startVertex, finishVertex);
        }

        void setName() {
            final String name = MessageFormat.format("Sapsan{0}", new Random().nextInt(100));
            sendMessage(ApiCommands.NAME, name);
            GameClient.this.name = name;
            LOGGER.info("I want my name was %s", name);
        }

        void knowNextVertices() {
            sendMessage(ApiCommands.NEXT_VERTICES, startVertex);
        }

        void move(final String vertexName) {
            LOGGER.info("Now I go to %s", vertexName);
            sendMessage(ApiCommands.MOVE, vertexName);
        }

        @Override
        public void run() {
            setName();
        }

    }

}
