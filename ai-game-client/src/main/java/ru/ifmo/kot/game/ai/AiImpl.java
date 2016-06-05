package ru.ifmo.kot.game.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.server.Server;
import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.aibase.AiBase;
import ru.ifmo.kot.game.client.GameClient;

import java.util.List;
import java.util.Random;

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
		final String currentVertex = api().currentVertex();
		LOGGER.info("I am %s and I call api for next vertices", Thread.currentThread().getName());
		final List<String> nextVertices = api().nextVertices(currentVertex);
		return nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size()));
	}

}
