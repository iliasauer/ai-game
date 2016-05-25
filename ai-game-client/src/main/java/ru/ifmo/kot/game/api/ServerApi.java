package ru.ifmo.kot.game.api;

public interface ServerApi {
    boolean move(final String vertexName);
    String[] nextVertices(final String vertex);
    int weight(final String vertex1, final String vertex2);
    String whereIsCompetitor(final int index);
    String whereIsCompetitor(final String id);
    String whereIsCompetitor();
}
