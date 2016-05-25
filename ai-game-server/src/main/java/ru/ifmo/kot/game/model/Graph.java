package ru.ifmo.kot.game.model;

import java.util.Set;

public interface Graph {

    int numberOfVertices();
    int numberOfEdges();
    Iterable<Edge> edges();
    int getWeight(final int srcVrtxIndx, final int dstVrtxIndx);
    boolean hasSpanningTree();
    boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);
    Set<Integer> nextVertices(final int vrtxIndx);

    String TYPE_KEY = "group";
    String CONTENT_KEY = "data";
    String NAME_KEY = "id";
    String SOURCE_KEY = "source";
    String TARGET_KEY = "target";
    String WEIGHT_KEY = "weight";

    String VERTEX = "nodes";
    String EDGE = "edges";

}
