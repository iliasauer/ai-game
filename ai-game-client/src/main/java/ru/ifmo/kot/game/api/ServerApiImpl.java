package ru.ifmo.kot.game.api;

import ru.ifmo.kot.api.ServerApi;

import java.util.List;

/**
 Created on 25.05.16.
 */
public class ServerApiImpl implements ServerApi {

	@Override
	public String startVertex() {
		return null;
	}

	@Override
	public String currentVertex() {
		return null;
	}

	@Override
	public String finishVertex() {
		return null;
	}

	@Override
	public List<String> nextVertices(final String vertex) {
		return null;
	}

	@Override
	public int weight(final String vertex1, final String vertex2) {
		return 0;
	}

	@Override
	public String whereIsCompetitor(final int index) {
		return null;
	}

	@Override
	public String whereIsCompetitor(final String id) {
		return null;
	}

	@Override
	public String whereIsCompetitor() {
		return null;
	}
}
