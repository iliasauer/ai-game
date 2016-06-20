package ru.ifmo.kot.game.model;

import java.util.Set;

public interface Graph {

    int numberOfVertices();
    int numberOfEdges();
    Iterable<Edge> edges();
    int getWeight(final int srcVrtxIndx, final int dstVrtxIndx);
    boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);
    Set<Integer> nextVertices(final int vrtxIndx);
    boolean hasSpanningTree();
    void fillGaps();
    int maxWeight();

}
