package ru.ifmo.kot.game;

import ru.ifmo.kot.game.elements.Field;
import ru.ifmo.kot.game.model.SymbolGraph;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 Created on 24.05.16.
 */
public class Game implements Api {

	private final Field field = new Field();

	@Override
	public boolean move(final String vertexName) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Set<String> nextVertices(final String vertex) {
		final SymbolGraph gameModel = field.getGameModel();
		final int vertexIndex = gameModel.index(vertex);
		final Set<Integer> nextVerticesIndices = gameModel.graph().nextVertices(vertexIndex);
		return nextVerticesIndices.stream()
			.collect(Collectors.mapping(gameModel::name, Collectors.toCollection(HashSet::new)));
	}

	@Override
	public int weight(final String vertex1, final String vertex2) {
		return field.getGameModel().getWeight(vertex1, vertex2);
	}

	@Override
	public String whereIsCompetitor(final int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String whereIsCompetitor(final String id) {
		throw new UnsupportedOperationException();
	}

	@Override
	public String whereIsCompetitor() {
		throw new UnsupportedOperationException();
	}
}
