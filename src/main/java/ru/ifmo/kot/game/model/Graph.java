package ru.ifmo.kot.game.model;

import ru.ifmo.kot.game.util.BinaryRandom;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Graph {
    private Map<Integer, Vertex> vertices;
    private List<Edge> edges;
    private int[][] adjacencyMatrix;
    private double coefficientOfEdgeNumber = 0.6;

    public Graph(final int numberOfVertices) {
        initAdjacencyMatrix(numberOfVertices);
    }

    public Graph(final List<String> verticesNames) {
        final int numberOfVertices = verticesNames.size();
        vertices = new HashMap<>(numberOfVertices);
        int index = 0;
        for (final String vertexName: verticesNames) {
            vertices.put(index++, new Vertex(vertexName));
        }
        initAdjacencyMatrix(numberOfVertices);
        final BinaryRandom random = new BinaryRandom(coefficientOfEdgeNumber);
        for (int i = 0; i < numberOfVertices - 1; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                if (random.nextBoolean()) {
                    putEdge(new Edge(i, j, 50));
                }
            }
        }
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
