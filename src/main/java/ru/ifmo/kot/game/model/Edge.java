package ru.ifmo.kot.game.model;

import java.text.MessageFormat;

public class Edge implements Comparable<Edge> {

    private Vertex sourceVertex;
    private Vertex destinationVertex;
    private int weight;

    public Edge(final Vertex sourceVertex, final Vertex destinationVertex, final int weight) {
        this.sourceVertex = sourceVertex;
        this.destinationVertex = destinationVertex;
        this.weight = weight;
    }

    public Vertex anyVertex() {
        return sourceVertex;
    }

    public Vertex otherVertex(final Vertex vertex) {
        if (vertex == sourceVertex) {
            return destinationVertex;
        }
        if (vertex == destinationVertex) {
            return sourceVertex;
        }
        return null;
    }

    @Override
    public int compareTo(Edge otherEdge) {
        if (weight < otherEdge.weight) {
            return -1;
        } else {
            if (weight > otherEdge.weight) {
                return +1;
            } else {
                return 0;
            }
        }
    }

    @Override
    public String toString() {
        return MessageFormat.format("{0} - {1} {2}",
                sourceVertex.name(), destinationVertex.name(), weight);
    }
}
