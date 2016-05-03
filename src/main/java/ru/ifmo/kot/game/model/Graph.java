package ru.ifmo.kot.game.model;

public interface Graph {

    int numberOfVertices();
    int numberOfEdges();
    Iterable<Edge> edges();
    int getWeight(final int srcVrtxIndx, final int dstVrtxIndx);
    boolean hasSpanningTree();
    boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);

}
