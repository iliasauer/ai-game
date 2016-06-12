package ru.ifmo.kot.game.visualiztion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.elements.Field;
import ru.ifmo.kot.game.server.GameServer;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created on 07.05.16.
 */
@ServerEndpoint(
	value = "/visual",
	encoders = {FieldEncoder.class}
)
public class VisualizationEndpoint {

	private static final Logger LOGGER = LogManager.getFormatterLogger(VisualizationEndpoint.class);
	private static final Queue<Session> VISUALIZERS = new ConcurrentLinkedQueue<>();

	@OnOpen
	public void addVisualiser(final Session session) {
		VISUALIZERS.offer(session);
		LOGGER.debug("The visualizer %s was added successfully", session.getId());
		final Field field = GameServer.game().field();
		sendMessage(field);
		sendMessage(field.getStartVerticesJson());
	}

	@OnClose
	public void removeVisualizer(final Session session) {
		VISUALIZERS.remove(session);
		LOGGER.debug("The visualizer %s was removed successfully", session.getId());
	}

	@SuppressWarnings("UnusedParameters")
	@OnError
	public void handleVisualizerError(final Session session, final Throwable error) {
		LOGGER.error("An error occurred on the %s visualizer", session.getId());
	}

	@OnMessage
	public void handleMessage(final Session session, final String message) {
		LOGGER.info("The visualizer %s: %s", session.getId(), message);
	}

	public static void sendMessage(final String message) {
		VISUALIZERS.stream().filter(Session:: isOpen).forEach(session -> {
			try {
				session.getBasicRemote().sendText(message);
			} catch(final IOException e) {
				LOGGER.error("Internal server error");
			}
		});
	}

	public static void sendMessage(final Field field) {
		VISUALIZERS.stream().filter(Session:: isOpen).forEach(session -> {
			try {
				session.getBasicRemote().sendObject(field);
			} catch(final IOException | EncodeException e) {
				LOGGER.error("Internal server error");
			}
		});
	}

}
