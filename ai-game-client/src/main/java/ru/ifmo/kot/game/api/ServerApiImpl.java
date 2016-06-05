package ru.ifmo.kot.game.api;

import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.client.GameClient;

import java.util.List;

/**
 Created on 25.05.16.
 */
public class ServerApiImpl implements ServerApi {

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
		return game.knowWeight
				(vertex1, vertex2);
	}

	@Override
	public List<String> whereAreCompetitors() {
		return null;
	}
}
