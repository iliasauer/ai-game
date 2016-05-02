package ru.ifmo.kot.game.model;

import ru.ifmo.kot.game.util.BinaryRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class UndirectedWeightedGraph implements Graph {

    private static final double COEFFICIENT_OF_EDGE_NUMBER = 0.3;
    private static final int WEIGHT_MULTIPLIER_UPPER_BOUND = 15;
    private static final int WEIGHT_MULTIPLIER_LOWER_BOUND = 13;
    private static final int WEIGHT_MULTIPLIER_RANGE =
            WEIGHT_MULTIPLIER_UPPER_BOUND - WEIGHT_MULTIPLIER_LOWER_BOUND;
    private static final BinaryRandom BINARY_RANDOM = new BinaryRandom(COEFFICIENT_OF_EDGE_NUMBER);
    private static final Random USUAL_RANDOM = new Random();

    private final int numberOfVertices;
    private int numberOfEdges;
    private List<Map<Integer, Integer>> adjacencyEdgeList;
    @SuppressWarnings("FieldCanBeLocal")

    UndirectedWeightedGraph(final int numberOfVertices) {
        this.numberOfVertices = numberOfVertices;
        this.numberOfEdges = 0;
        adjacencyEdgeList = new ArrayList<>(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            adjacencyEdgeList.add(new HashMap<>());
        }
        for (int srcVrtxIndex = 0; srcVrtxIndex < numberOfVertices - 1; srcVrtxIndex++) {
            for (int dstVrtxIndx = srcVrtxIndex + 1; dstVrtxIndx < numberOfVertices; dstVrtxIndx++) {
                if (BINARY_RANDOM.nextBoolean()) {
                    final int weight = nextWeight(srcVrtxIndex, dstVrtxIndx);
                    putEdge(srcVrtxIndex, dstVrtxIndx, weight);
                }
            }
        }
    }

    @Override
    public int numberOfVertices() {
        return numberOfVertices;
    }

    @Override
    public int numberOfEdges() {
        return numberOfEdges;
    }

    @Override
    public void putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight) {
        adjacencyEdgeList.get(srcVrtxIndx).put(dstVrtxIndx, weight);
        adjacencyEdgeList.get(dstVrtxIndx).put(srcVrtxIndx, weight);
        numberOfEdges++;
    }

    @Override
    public Iterable<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (int srcVrtxIndx = 0; srcVrtxIndx < adjacencyEdgeList.size(); srcVrtxIndx++) {
            for (final Map.Entry<Integer, Integer> edgePair :
                    adjacencyEdgeList.get(srcVrtxIndx).entrySet()) {
                final int dstVrtxIndx = edgePair.getKey();
                final int weight = edgePair.getValue();
                if (srcVrtxIndx < dstVrtxIndx){
                    edges.add(new Edge(srcVrtxIndx, dstVrtxIndx, weight));
                }
            }
        }
        return edges;
    }

    private int deltaIndex(final int srcVrtxIndx, final int dstVrtxIndx) {
        return Math.abs(srcVrtxIndx - dstVrtxIndx);
    }

    private int nextWeight(final int srcVrtxIndx, final int dstVrtxIndx) {
        return (USUAL_RANDOM.nextInt(WEIGHT_MULTIPLIER_RANGE + 1) + WEIGHT_MULTIPLIER_LOWER_BOUND)
                *
                deltaIndex(srcVrtxIndx, dstVrtxIndx);
    }

}
