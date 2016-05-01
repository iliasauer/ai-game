package ru.ifmo.kot.game.model;

import org.apache.commons.lang3.tuple.ImmutablePair;
import ru.ifmo.kot.game.util.BinaryRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UndirectedWeightedGraph {

    private int numberOfVertices;
    private Map<Integer, Vertex> vertices;
    private Map<Map.Entry<Integer, Integer>, Edge> edges;
    private int[][] adjacencyMatrix;
    private double coefficientOfEdgeNumber = 0.6;

    public UndirectedWeightedGraph(final int numberOfVertices) {
        initAdjacencyMatrix(numberOfVertices);
    }

    public UndirectedWeightedGraph(final List<String> verticesNames) {
        numberOfVertices = verticesNames.size();
        vertices = new HashMap<>(numberOfVertices);
        int index = 0;
        for (final String vertexName: verticesNames) {
            vertices.put(index, new Vertex(vertexName));
            index++;
        }
        initAdjacencyMatrix(numberOfVertices);
        final BinaryRandom random = new BinaryRandom(coefficientOfEdgeNumber);
        index = 0;
        for (int i = 0; i < numberOfVertices - 1; i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
                if (random.nextBoolean()) {
                    final int weight = 29;
                    putEdge(i, j, weight);;
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

    public void putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight) {
        final Vertex srcVertex = getVertex(srcVrtxIndx);
        final Vertex dstVertex = getVertex(dstVrtxIndx);
        final Edge edge = new Edge(srcVertex, dstVertex, weight);
        edges.put(new ImmutablePair<>(srcVrtxIndx, dstVrtxIndx), edge);
        adjacencyMatrix[srcVrtxIndx][dstVrtxIndx] =
        adjacencyMatrix[dstVrtxIndx][srcVrtxIndx] = weight;
    }

    public int getWeight(final int srcVrtxIndx,
                         final int dstVrtxIndx) {
        return adjacencyMatrix[srcVrtxIndx][dstVrtxIndx];
    }

    public boolean areConnected(final int srcVrtxIndx,
                                final int dstVrtxIndx) {
        return getWeight(srcVrtxIndx, dstVrtxIndx) > 0;
    }

    public Vertex getVertex(final int index) {
        return vertices.get(index);
    }

    public void printVertices() {
        for (final Map.Entry<Integer, Vertex> vertexEntry: vertices.entrySet()) {
            System.out.println(vertexEntry.getKey() + ": " + vertexEntry.getValue());
        }
    }

    public void printEdges() {
        for (int i = 0; i < vertices.size(); i++) {
            for (int j = i + 1; j < numberOfVertices; j++) {
//                edges.get()
            }
        }
    }

    public List<Edge> adjacentEdges() {
        return null;
    }

    public Iterable<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (final Vertex vertex: vertices.values()) {
            for (final Edge adjacentEdge: adjacentEdges()) {
//                if (adjacentEdge.otherVertex(vertex) > vertex)
            }
        }
        return null;
    }
}
