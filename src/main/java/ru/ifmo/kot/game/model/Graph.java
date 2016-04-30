package ru.ifmo.kot.game.model;

public class Graph {

    private int[][] adjacencyMatrix;

    public Graph(final int numberOfVertices) {
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

    public void putEdge(final int sourceVertexIndex,
                        final int destinationVertexIndex,
                        final int weight) {
        adjacencyMatrix[sourceVertexIndex][destinationVertexIndex] =
        adjacencyMatrix[destinationVertexIndex][sourceVertexIndex] = weight;
    }

    public int getWeight(final int sourceVertexIndex,
                         final int destinationVertexIndex) {
        return adjacencyMatrix[sourceVertexIndex][destinationVertexIndex];
    }

    public boolean areConnected(final int sourceVertexIndex,
                                final int destinationVertexIndex) {
        return getWeight(sourceVertexIndex, destinationVertexIndex) > 0;
    }
}
