package ru.ifmo.kot.game.model;

public interface Graph {

    int numberOfVertices();
    int numberOfEdges();
    void putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);
    Iterable<Edge> edges();

}
