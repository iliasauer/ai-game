package ru.ifmo.kot.game;

import java.util.Set;

/**
 Created on 24.05.16.
 */
public interface Api {
	boolean move(final String playerName, final String vertexName);
	Set<String> nextVertices(final String vertex);
	int weight(final String vertex1, final String vertex2);
//	String whereIsCompetitor(final int index);
	String whereIsCompetitor(final String id);
//	String whereIsCompetitor();
}
