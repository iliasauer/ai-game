package ru.ifmo.kot.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

class UndirectedWeightedGraph implements Graph {

    private static final int WEIGHT_MULTIPLIER_UPPER_BOUND = 15;
    private static final int WEIGHT_MULTIPLIER_LOWER_BOUND = 13;
    private static final int WEIGHT_MULTIPLIER_RANGE =
            WEIGHT_MULTIPLIER_UPPER_BOUND - WEIGHT_MULTIPLIER_LOWER_BOUND;
    private static final Random USUAL_RANDOM = new Random();

    private final int numberOfVertices;
    private int numberOfEdges;
    private List<Map<Integer, Integer>> adjacencyEdgeList;

    UndirectedWeightedGraph(final int numberOfVertices) {
        if (numberOfVertices < 1) {
            throw new IllegalArgumentException();
        }
        this.numberOfVertices = numberOfVertices;
        this.numberOfEdges = 0;
        adjacencyEdgeList = new ArrayList<>(numberOfVertices);
        for (int i = 0; i < numberOfVertices; i++) {
            adjacencyEdgeList.add(new HashMap<>());
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
    public boolean putEdge(final int srcVrtxIndx, final int dstVrtxIndx, final int weight) {
        if (srcVrtxIndx < numberOfVertices && dstVrtxIndx < numberOfVertices) {
            adjacencyEdgeList.get(srcVrtxIndx).put(dstVrtxIndx, weight);
            adjacencyEdgeList.get(dstVrtxIndx).put(srcVrtxIndx, weight);
            numberOfEdges++;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public Iterable<Edge> edges() {
        List<Edge> edges = new ArrayList<>();
        for (int srcVrtxIndx = 0; srcVrtxIndx < adjacencyEdgeList.size(); srcVrtxIndx++) {
            for (final Map.Entry<Integer, Integer> edgePair :
                    adjacencyEdgeList.get(srcVrtxIndx).entrySet()) {
                final int dstVrtxIndx = edgePair.getKey();
                final int weight = edgePair.getValue();
                if (srcVrtxIndx < dstVrtxIndx) {
                    edges.add(new Edge(srcVrtxIndx, dstVrtxIndx, weight));
                }
            }
        }
        return edges;
    }

    @Override
    public int maxWeight() {
        int maxWeight = 0;
        final Iterable<Edge> edges = edges();
        for (final Edge edge: edges) {
            final int weight = edge.weight();
            if (weight > maxWeight) {
                maxWeight = weight;
            }
        }
        return maxWeight;
    }

    @Override
    public int getWeight(final int srcVrtxIndx, final int dstVrtxIndx) {
        final Map<Integer, Integer> nextVerticesWeights = adjacencyEdgeList.get(srcVrtxIndx);
        if (nextVerticesWeights.containsKey(dstVrtxIndx)) {
            return nextVerticesWeights.get(dstVrtxIndx);
        } else {
            return -1;
        }
    }

    private int deltaIndex(final int srcVrtxIndx, final int dstVrtxIndx) {
        return Math.abs(srcVrtxIndx - dstVrtxIndx);
    }

    private int nextWeight(final int srcVrtxIndx, final int dstVrtxIndx) {
        return (USUAL_RANDOM.nextInt(WEIGHT_MULTIPLIER_RANGE + 1) + WEIGHT_MULTIPLIER_LOWER_BOUND)
                *
                deltaIndex(srcVrtxIndx, dstVrtxIndx);
    }

    @Override
    public Set<Integer> nextVertices(final int vrtxIndx) {
        return adjacencyEdgeList.get(vrtxIndx).keySet();
    }

}
