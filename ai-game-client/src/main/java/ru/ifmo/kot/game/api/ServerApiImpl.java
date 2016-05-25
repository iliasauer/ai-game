package ru.ifmo.kot.game.api;

/**
 Created on 25.05.16.
 */
public class ServerApiImpl
	implements ServerApi {
	
	@Override
	public boolean move(final String vertexName) {
		return false;
	}

	@Override
	public String[] nextVertices(final String vertex) {
		return new String[0];
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
