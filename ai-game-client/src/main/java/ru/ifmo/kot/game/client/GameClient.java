package ru.ifmo.kot.game.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.tools.ApiCommands;
import ru.ifmo.kot.tools.Messenger;

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
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@ClientEndpoint(encoders = {Messenger.MessageEncoder.class}, decoders = {Messenger.MessageDecoder.class})
public class GameClient {

	private static final Logger LOGGER = LogManager.getFormatterLogger(GameClient.class);
	private static final String GREETING = "The connection is open";
	private Session session;
	private Object response;
	private Game game = new Game();

	@OnOpen
	public void greetServer(final Session session) {
		this.session = session;
		try {
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			executor.submit(game);
			LOGGER.info("Game started");
		} catch(Exception e) {
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
		final String participant = message.getParticipant();
		switch(command) {
			case ApiCommands.WEIGHT:
				this.response = message.getArgs()[0];
				break;
			case ApiCommands.NAME:
				this.response = message.getArgs()[0];
				break;
			case ApiCommands.START_DATA:
				final String startVertex = (String) message.getArgs()[0];
				final String finishVertex = (String) message.getArgs()[1];
				game.initStartVertices(startVertex, finishVertex);
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

	private Future<Object> sendMessage(final Messenger.Message message)
	throws IOException, EncodeException, InterruptedException, ExecutionException {
		if(session.isOpen()) {
			session.getBasicRemote().sendObject(message);
			final ExecutorService executor = Executors.newSingleThreadExecutor();
			final Future<Object> future = executor.submit(() -> {
				while(GameClient.this.response == null) {
					TimeUnit.MILLISECONDS.sleep(10);
				}
				return GameClient.this.response;
			});
			return future;
		} else {
			LOGGER.error("Failed to send message to the server");
		}
		return null;
	}

	public static void main(String[] args)
	throws InterruptedException {
		final WebSocketContainer webSocketContainer = ContainerProvider.getWebSocketContainer();
		webSocketContainer.setDefaultMaxSessionIdleTimeout(TimeUnit.SECONDS.toMillis(30));
		try {
			final Session server = webSocketContainer.connectToServer(GameClient.class,
				URI.create(ClientConstants.SERVER_URL)
			); // todo check is it the same session
			TimeUnit.SECONDS.sleep(30);
		} catch(DeploymentException | IOException e) {
			LOGGER.error("Failed to connect to the server");
		}
	}

	private class Game implements Runnable {

		private String startVertex;
		private String finishVertex;

		void initStartVertices(final String startVertex, final String finishVertex) {
			this.startVertex = startVertex;
			this.finishVertex = finishVertex;
			LOGGER.info("Vertices are initialized: %s-%s", startVertex, finishVertex);
		}


		@Override
		public void run() {
			try {
				final String name = "Sapsan" + new Random().nextInt(100);
				sendMessage(new Messenger.Message("player1", ApiCommands.NAME, name));
				LOGGER.info("I want my name was %s", name);
			} catch(IOException | EncodeException | InterruptedException | ExecutionException e) {
				e.printStackTrace();
			}
		}

	}

}
