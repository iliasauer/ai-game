package ru.ifmo.kot.game.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<Integer, Vertex> vertices;
    private List<Edge> edges;
    private int[][] adjacencyMatrix;

    public Graph(final int numberOfVertices) {
        initAdjacencyMatrix(numberOfVertices);
    }

    public Graph(final List<String> verticesNames) {
        vertices = new HashMap<>(verticesNames.size());
        int index = 0;
        for (final String vertexName: verticesNames) {
            vertices.put(index++, new Vertex(vertexName));
        }
        initAdjacencyMatrix(verticesNames.size());
    }

    private void initAdjacencyMatrix(final int numberOfVertices) {
        adjacencyMatrix = new int[numberOfVertices][numberOfVertices];
    }

    public void printAdjacencyMatrix() {
        for (int[] row: adjacencyMatrix) {
            for (int value: row) {
                System.out.print(value + " ");
            }
            System.out.println();
        }
    }

    public void putEdge(final Edge edge) {
        final int srcVrtxIndx = edge.getSourceVertexIndex();
        final int dstVrtxIndx = edge.getDestinationVertexIndex();
        adjacencyMatrix[srcVrtxIndx][dstVrtxIndx] =
        adjacencyMatrix[dstVrtxIndx][srcVrtxIndx] = edge.getWeight();
    }

    public int getWeight(final int sourceVertexIndex,
                         final int destinationVertexIndex) {
        return adjacencyMatrix[sourceVertexIndex][destinationVertexIndex];
    }

    public boolean areConnected(final int sourceVertexIndex,
                                final int destinationVertexIndex) {
        return getWeight(sourceVertexIndex, destinationVertexIndex) > 0;
    }

    public Map<Integer, Vertex> getVertices() {
        return vertices;
    }

    public Vertex getVertex(final int index) {
        return vertices.get(index);
    }

    public void printVertices() {
        for (final Map.Entry<Integer, Vertex> vertexEntry: vertices.entrySet()) {
            System.out.println(vertexEntry.getKey() + ": " + vertexEntry.getValue());
        }
    }
}
