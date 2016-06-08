package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.aibase.Ai;
import ru.ifmo.kot.game.ai.AiImpl;
import ru.ifmo.kot.game.api.ServerApiImpl;
import ru.ifmo.kot.protocol.Command;
import ru.ifmo.kot.protocol.Messenger;
import ru.ifmo.kot.protocol.RequestStatus;
import ru.ifmo.kot.api.SendMessageTask;

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
import java.util.Collections;
import java.util.HashMap;
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

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

	private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
	private static final Random USUAL_RANDOM = new Random();
	private static final Map<Session, String> SERVER_SESSIONS = new HashMap<>(2);
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
			); // todo check is it the same serverSession?
			TimeUnit.SECONDS.sleep(30);
		} catch(DeploymentException | IOException e) {
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
		} catch(final Exception e) {
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
		final Optional<RequestStatus> optionalRequestStatus = message.getRequestStatus();
		final RequestStatus requestStatus;
		switch(command) {
			case NAME:
				if(optionalRequestStatus.isPresent()) {
					requestStatus = optionalRequestStatus.get();
				} else {
					LOGGER.error("The wrong server message format");
					throw new IllegalArgumentException();
				}
				switch(requestStatus) {
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
			case MOVE:
				if(optionalRequestStatus.isPresent()) {
					requestStatus = optionalRequestStatus.get();
				} else {
					LOGGER.error("The wrong server message format");
					throw new IllegalArgumentException();
				}
				switch(requestStatus) {
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
			case NEXT_VERTICES:
				if(optionalRequestStatus.isPresent()) {
					requestStatus = optionalRequestStatus.get();
				} else {
					LOGGER.error("The wrong server message format");
					throw new IllegalArgumentException();
				}
				switch(requestStatus) {
					case OK:
						responseMap.put(Command.NEXT_VERTICES.name(), message.getArgs()[0]);
						break;
				}
				break;
			case WEIGHT:
				if(optionalRequestStatus.isPresent()) {
					requestStatus = optionalRequestStatus.get();
				} else {
					LOGGER.error("The wrong server message format");
					throw new IllegalArgumentException();
				}
				switch(requestStatus) {
					case OK:
						final int weight = (Integer) message.getArgs()[0];
						responseMap.put(Command.WEIGHT.name(), weight);
						break;
				}
				break;
			default:
		}
	}

	@OnError
	public void handleError(final Session session, final Throwable error) {
		LOGGER.debug("An error occurred", error);
	}

	private SendMessageTask<?> getSendMessageTask(
		final Command command, final Object... args
	) {
		return new SendMessageTask<>(Collections.singletonList(serverSession), responseMap,
			command.name(), session -> sendMessage(command, args)
		);
	}

	@SuppressWarnings("unchecked")
	private void sendMessage(final Messenger.Message message) {
		if(serverSession.isOpen()) {
			serverSession.getAsyncRemote().sendObject(message);
		} else {
			LOGGER.error("Failed to send the message to the server");
		}
	}

	private void sendMessage(final Command command, final Object... args) {
		sendMessage(new Messenger.Message(command, args));
	}

	public class Game
		implements Runnable {

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

		@SuppressWarnings("unchecked")
		public List<String> knowNextVertices() {
			return knowNextVertices(startVertex);
		}

		@SuppressWarnings("unchecked")
		public List<String> knowNextVertices(final String vertexName) {
			final Future<Void> future =
				executor.submit(getSendMessageTask(Command.NEXT_VERTICES, vertexName));
			LOGGER.info("Send NEXT_VERTICES request");
			try {
				future.get();
			} catch(final InterruptedException | ExecutionException e) {
				LOGGER.error("Internal server error");
			}
			return (List<String>) responseMap.get(Command.NEXT_VERTICES.name());
		}

		public int knowWeight(final String vertexName1, final String vertexName2) {
			final Future<Void> future =
				executor.submit(getSendMessageTask(Command.WEIGHT, vertexName1, vertexName2));
			LOGGER.info("Send WEIGHT request");
			try {
				future.get();
			} catch(final InterruptedException | ExecutionException e) {
				LOGGER.error("Internal server error");
			}
			return (Integer) responseMap.get(Command.WEIGHT.name());
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
			Executors.newSingleThreadExecutor().submit(() -> {
				final String s = ai.move();
				sendMessage(Command.MOVE, s);
			});
		}

		void moveAgain(final String oldMove) {
		}

		@Override
		public void run() {
			//            nameMe(name);
		}
	}
}
