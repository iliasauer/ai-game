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
import ru.ifmo.kot.protocol.Command;
import ru.ifmo.kot.util.EmbeddedLogger;
import ru.ifmo.kot.protocol.Messenger;
import ru.ifmo.kot.protocol.RequestStatus;
import ru.ifmo.kot.api.SendMessageTask;

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import static ru.ifmo.kot.game.server.ServerConstants.CONTEXT_PATH;
import static ru.ifmo.kot.game.server.ServerConstants.NUM_OF_CLIENTS;
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
	private static Map<String, Void> turnMap = new LinkedHashMap<>();
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
		} catch(Throwable e) {
			LOGGER.info("The game server cannot be started.");
		}
	}

	@OnOpen
	public void addClient(final Session client) {
		this.localClient = client;
		if(clients.size() < ServerConstants.NUM_OF_CLIENTS) {
			clients.add(client);
			LOGGER.info("The player has successfully joined");
			if(clients.size() == ServerConstants.NUM_OF_CLIENTS) {
				LOGGER.info("It has enough players joined. The game initialization is started.");
				nameInvite();
			}
		} else {
			try {
				client.close();
				LOGGER.info("It has enough players joined");
			} catch(final IOException e) {
				LOGGER.error("Failed to close the session");
			}
		}
	}

	@OnClose
	public void removeClient(final Session session) {
		LOGGER.debug("The client session %s was removed successfully", session.getId());
	}

	@SuppressWarnings("UnusedParameters")
	@OnError
	public void handleClientError(final Session session, final Throwable error) {
		LOGGER.error("An error occurred with %s", GAME.getName(session.getId()));
		removeClient(session);
	}

	@OnMessage
	public void handleMessage(final Messenger.Message message, final Session session) {
		final Object[] args = message.getArgs();
		switch(message.getCommand()) {
			case NAME:
				nameResponse(session, (String) args[0]); // name
				break;
			case MOVE:
				moveResponse(session, (String) args[0]); // vertex
				break;
			case WEIGHT:
				weightResponse((String) args[0], (String) args[1]); // vertex1, vertex2
				break;
			case NEXT_VERTICES:
				nextVerticesResponse((String) args[0]); // vertex
				break;
			case COMPETITORS_POSITIONS:
				competitorsPositionsResponse();
				break;
			default:
				unrecognizableResponse(message.getCommand());
		}
	}

	private static void sendMessage(final Session session, final Messenger.Message message) {
		if(session.isOpen()) {
			try {
				session.getBasicRemote().sendObject(message);
			} catch(final IOException | EncodeException e) {
				LOGGER.error("Failed to send a message to the client");
			}
		}
	}

	private void sendMessage(final Messenger.Message message) {
		if(localClient.isOpen()) { // todo check the need
			try {
				localClient.getBasicRemote().sendObject(message);
			} catch(final IOException | EncodeException e) {
				LOGGER.error("Failed to send a message to the client");
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
		final Session session, final Command command, final Object... args
	) {
		sendMessage(session, new Messenger.Message(command, args));
	}

	private void sendMessage(
		final Command command, final RequestStatus status, final Object... args
	) {
		sendMessage(new Messenger.Message(command, status, args));
	}

	private static SendMessageTask<Void> getSendMessageTask(final Command command) {
		return new SendMessageTask<>(clients, turnMap, null, session -> {
			sendMessage(session, command, RequestStatus.INVITE);
			LOGGER.info("Sent %s invite to %s", command, GAME.getName(session.getId()));
		});
	}

	private void nameInvite() {
		INVITE_EXECUTOR.submit(getSendMessageTask(Command.NAME));
	}

	private void moveInvite() {
		INVITE_EXECUTOR.submit(getSendMessageTask(Command.MOVE));
	}

	private void nameResponse(final Session client, final String name) {
		final String clientId = client.getId();
		if(GAME.name(clientId, name)) {
			sendMessage(client, Command.NAME, RequestStatus.OK);
			turnMap.put(clientId, null);
			sendMessage(client, Command.START_DATA, GAME.startVertex(), GAME.finishVertex());
		} else {
			sendMessage(client, Command.NAME, RequestStatus.FAIL, name);
		}
		if(turnMap.size() == NUM_OF_CLIENTS) {
			moveInvite();
		}
	}

	private void moveResponse(final Session client, final String nextVertexName) {
		if(GAME.move(client.getId(), nextVertexName)) {
			sendMessage(client, Command.MOVE, RequestStatus.OK);
			turnMap.put(client.getId(), null);
		} else {
			sendMessage(client, Command.MOVE, RequestStatus.FAIL, nextVertexName);
		}
		if(turnMap.size() == NUM_OF_CLIENTS) {
			moveInvite();
		}
	}

	@SuppressWarnings("ConfusingArgumentToVarargsMethod")
	private void nextVerticesResponse(final String currentVertex) {
		final Optional<Set<String>> nextVerticesOptionalSet =
			Optional.of(GAME.nextVertices(currentVertex));
		if(nextVerticesOptionalSet.isPresent()) {
			final Set<String> nextVerticesSet = nextVerticesOptionalSet.get();
			String[] nextVertices = new String[nextVerticesSet.size()];
			nextVerticesSet.toArray(nextVertices);
			sendMessage(Command.NEXT_VERTICES, RequestStatus.OK, nextVerticesOptionalSet.get());
		} else {
			sendMessage(Command.NEXT_VERTICES, RequestStatus.FAIL, currentVertex);
		}
	}

	@SuppressWarnings("ConstantConditions")
	private void weightResponse(final String vrtx1, final String vrtx2) {
		final Optional<Integer> optionalWeight = Optional.of(GAME.weight(vrtx1, vrtx2));
		if(optionalWeight.isPresent()) {
			sendMessage(Command.WEIGHT, RequestStatus.OK, optionalWeight.get());
		} else {
			sendMessage(Command.WEIGHT, RequestStatus.FAIL, vrtx1, vrtx2);
		}
	}

	private void competitorsPositionsResponse() {
		sendMessage(Command.COMPETITORS_POSITIONS, RequestStatus.OK, GAME.competitorsPositions());
	}

	private void unrecognizableResponse(final Command command) {
		LOGGER.error("The command is not supported by the server");
		sendMessage(Command.UNRECOGNIZABLE, RequestStatus.FAIL, command.name());
	}

	private static class Game {

		private Map<String, Player> players = new LinkedHashMap<>();
		private final Field field = new Field();
		private final String startVertex = startVertices()[0];
		private final String finishVertex = startVertices()[1];

		String getName(final String sessionId) {
			final Player player = players.get(sessionId);
			if(!Objects.isNull(player)) {
				return player.getName();
			} else {
				return sessionId;
			}
		}

		private boolean isNameOccupied(final String name) {
			return players.values().stream().anyMatch(player -> player.getName().equals(name));
		}

		private boolean name(final String clientId, final String name) {
			if(!isNameOccupied(name)) {
				players.put(clientId, new Player(name, startVertex));
				LOGGER.info("There is %s name for session %s", name, clientId);
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

		boolean move(final String clientId, final String nextVertex) {
			if(field.doesVertexExist(nextVertex)) {
				final Player player = players.get(clientId);
				final String currentVertex = player.getCurrentPosition();
				if(field.doesEdgeExist(currentVertex, nextVertex)) {
					player.setCurrentPosition(nextVertex);
					return true;
				} else {
					LOGGER.info(
						"There is no edge between vertices %s and %s", currentVertex, nextVertex);
				}
			} else {
				LOGGER.info("The vertex %s does not exist", nextVertex);
			}
			return false;
		}

		Set<String> nextVertices(final String vertex) {
			if(field.doesVertexExist(vertex)) {
				return field.getNextVertices(vertex);
			} else {
				return null;
			}
		}

		Integer weight(final String vrtx1, final String vrtx2) {
			if(field.doesVertexExist(vrtx1)) {
				if(field.doesVertexExist(vrtx2)) {
					if(!field.doesEdgeExist(vrtx1, vrtx2)) {
						LOGGER.info("There is no edge between vertices %s and %s", vrtx1, vrtx2);
					}
					return field.getGameModel().getWeight(vrtx1, vrtx2);
				}
			}
			return null;
		}

		Map<String, String> competitorsPositions() {
			return players.values().stream()
				.collect(Collectors.toMap(
					Player:: getName,
					Player:: getCurrentPosition,
					(position1, position2) -> {
						LOGGER.error("The player position's collision");
						return "collision";
					}
				));
		}
	}
}
