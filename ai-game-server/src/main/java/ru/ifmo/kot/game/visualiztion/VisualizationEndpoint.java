package ru.ifmo.kot.game.visualiztion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.elements.Field;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 07.05.16.
 */
@ServerEndpoint(value = "/visual")
public class VisualizationEndpoint {

	private static final Logger LOGGER = LogManager.getFormatterLogger(VisualizationEndpoint.class);
	private static final Queue<Session> VISUALIZERS = new ConcurrentLinkedQueue<>();

	@OnOpen
	public void addVisualiser(final Session session) {
		VISUALIZERS.offer(session);
		final Field field = new Field();
		try {
			sendMessage(field.getGameModelAsJson().toString());
		} catch (IOException e) {
			LOGGER.error("Failed to show the game field");
		}
		LOGGER.debug("The visualizer %s was added successfully", session.getId());
	}

	@OnClose
	public void removeVisualizer(final Session session) {
		VISUALIZERS.remove(session);
		LOGGER.debug("The visualizer %s was removed successfully", session.getId());
	}

	@OnError
	public void handleVisualizerError(final Session session, final Throwable error) {
		LOGGER.error("An error occurred on the %s visualizer", session.getId());
	}

	@OnMessage
	public void handleMessage(final Session session, final String message) {
		LOGGER.info("The visualizer %s: %s", session.getId(), message);
	}

	public static void sendMessage(final String message) throws IOException {
		for (final Session session: VISUALIZERS) {
			if (session.isOpen()) {
				session.getBasicRemote().sendText(message);
			}
		}
	}

}
