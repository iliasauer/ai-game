package ru.ifmo.kot.game.visualiztion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private final Queue<Session> visualizers = new ConcurrentLinkedQueue<>();

	@OnOpen
	public void addVisualiser(final Session session) {
		visualizers.offer(session);
		LOGGER.debug("The visualizer %s was added successfully", session.getId());
	}

	@OnClose
	public void removeVisualizer(final Session session) {
		visualizers.remove(session);
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

	public void sendMessage(final String message) throws IOException {
		for (final Session session: visualizers) {
			if (session.isOpen()) {
				session.getBasicRemote().sendText(message);
			} else {
				removeVisualizer(session);
			}
		}
	}

}
