package ru.ifmo.kot.game.model;

public interface ExtendedGraph extends Graph {

    boolean hasSpanningTree();
    boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight);


}
