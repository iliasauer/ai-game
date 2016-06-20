package ru.ifmo.kot.game.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.client.GameClient;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 Created on 25.05.16.
 */
public class ServerApiImpl implements ServerApi {

	private static final Logger LOGGER = LogManager.getFormatterLogger(ServerApiImpl.class);

	private final GameClient.Game game;

	public ServerApiImpl(final GameClient.Game game) {
		this.game = game;
	}

	@Override
	public String startVertex() {
		return game.startVertex();
	}

	@Override
	public String currentVertex() {
		return game.currentVertex();
	}

	@Override
	public String finishVertex() {
		return game.finishVertex();
	}

	@Override
	public List<String> nextVertices(final String vertex) {
		return game.knowNextVertices(vertex);
	}

	@Override
	public int weight(final String vertex1, final String vertex2) {
		return game.knowWeight(vertex1, vertex2);
	}

	@Override
	public Map<String, String> whereAreCompetitors() {
		return game.knowWhereAreCompetitors();
	}
}
