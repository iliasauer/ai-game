package ru.ifmo.kot.api;

import java.util.List;

public interface ServerApi {
    String startVertex();
    String currentVertex();
    String finishVertex();
    List<String> nextVertices(final String vertex);
    int weight(final String vertex1, final String vertex2);
    String whereIsCompetitor(final int index);
    String whereIsCompetitor(final String id);
    String whereIsCompetitor();
}
