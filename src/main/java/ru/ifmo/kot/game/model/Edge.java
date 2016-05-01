package ru.ifmo.kot.game.model;

import ru.ifmo.kot.game.elements.Obstacle;

public class Edge {

    private final int sourceVertexIndex;
    private final int destinationVertexIndex;
    private final int weight;
    private final Obstacle obstacle;

    public Edge(int sourceVertexIndex, int destinationVertexIndex, int weight) {
        this.sourceVertexIndex = sourceVertexIndex;
        this.destinationVertexIndex = destinationVertexIndex;
        this.weight = weight;
        obstacle = new Obstacle();
    }

    public int getSourceVertexIndex() {
        return sourceVertexIndex;
    }

    public int getDestinationVertexIndex() {
        return destinationVertexIndex;
    }

    public int getWeight() {
        return weight;
    }
}
