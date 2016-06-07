package ru.ifmo.kot.game.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.aibase.AiBase;
import ru.ifmo.kot.game.client.GameClient;

import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 Created on 29.05.16.
 */
public class AiImpl extends AiBase {

	private static final Random USUAL_RANDOM = new Random();
	private static final Logger LOGGER = LogManager.getFormatterLogger(AiImpl.class);

	public AiImpl(final ServerApi api) {
		super(api);
	}

	@Override
	public String move() {
		final String startVertex = api().startVertex();
		final String currentVertex = api().currentVertex();
		final String finishVertex = api().finishVertex();
		final List<String> nextVertices = api().nextVertices(currentVertex);
		final int weight = api().weight(currentVertex, finishVertex);
		return nextVertices.get(0);
	}

}
