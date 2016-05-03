package ru.ifmo.kot.game.model;

import java.util.Set;

public interface Graph {

    int numberOfVertices();
    int numberOfEdges();
    boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);
    Iterable<Edge> edges();
    int getWeight(final int srcVrtxIndx, final int dstVrtxIndx);
    boolean hasSpanningTree();

}
