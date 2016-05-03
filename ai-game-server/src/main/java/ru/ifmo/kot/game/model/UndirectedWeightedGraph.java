package ru.ifmo.kot.game.model;

import ru.ifmo.kot.game.util.BinaryRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class UndirectedWeightedGraph implements Graph {

    private static final double COEFFICIENT_OF_EDGE_NUMBER = 0.1;
    private static final int WEIGHT_MULTIPLIER_UPPER_BOUND = 15;
    private static final int WEIGHT_MULTIPLIER_LOWER_BOUND = 13;
    private static final int WEIGHT_MULTIPLIER_RANGE =
            WEIGHT_MULTIPLIER_UPPER_BOUND - WEIGHT_MULTIPLIER_LOWER_BOUND;
    private static final BinaryRandom BINARY_RANDOM = new BinaryRandom(COEFFICIENT_OF_EDGE_NUMBER);
    private static final Random USUAL_RANDOM = new Random();

    private final int numberOfVertices;
    private int numberOfEdges;
    private List<Map<Integer, Integer>> adjacencyEdgeList;
    private Set<Integer> mainVerticesTree;

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
        for (int srcVrtxIndex = 0; srcVrtxIndex < numberOfVertices - 1; srcVrtxIndex++) {
            for (int dstVrtxIndx = srcVrtxIndex + 1; dstVrtxIndx < numberOfVertices; dstVrtxIndx++) {
                if (BINARY_RANDOM.nextBoolean()) {
                    final int weight = nextWeight(srcVrtxIndex, dstVrtxIndx);
                    putEdge(srcVrtxIndex, dstVrtxIndx, weight);
                }
            }
        }
        collectMainTreeVertices();
        if (! hasSpanningTree()) {
            fillGaps();
            collectMainTreeVertices();
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
    public int getWeight(final int srcVrtxIndx, final int dstVrtxIndx) {
        return adjacencyEdgeList.get(srcVrtxIndx).get(dstVrtxIndx);
    }

    private int deltaIndex(final int srcVrtxIndx, final int dstVrtxIndx) {
        return Math.abs(srcVrtxIndx - dstVrtxIndx);
    }

    private int nextWeight(final int srcVrtxIndx, final int dstVrtxIndx) {
        return (USUAL_RANDOM.nextInt(WEIGHT_MULTIPLIER_RANGE + 1) + WEIGHT_MULTIPLIER_LOWER_BOUND)
                *
                deltaIndex(srcVrtxIndx, dstVrtxIndx);
    }

    private void collectMainTreeVertices() {
        Set<Integer> mainVerticesTree = new HashSet<>();
        Set<Integer> checkedKeys = new HashSet<>();
        mainVerticesTree.add(0);
        mainVerticesTree.addAll(adjacencyEdgeList.get(0).keySet());
        checkedKeys.add(0);
        while (checkedKeys.size() < mainVerticesTree.size()) {
            Set<Integer> keySet = new HashSet<>();
            for (Integer key : mainVerticesTree) {
                if (! checkedKeys.contains(key)) {
                    keySet.addAll(adjacencyEdgeList.get(key).keySet());
                    checkedKeys.add(key);
                }
            }
            mainVerticesTree.addAll(keySet);
        }
        this.mainVerticesTree = mainVerticesTree;
    }

    @Override
    public boolean hasSpanningTree() {
        return mainVerticesTree.size() == numberOfVertices;
    }

    private void fillGaps() {
        if (numberOfVertices < 2) {
            return;
        }
        if (! mainVerticesTree.contains(0)) {
            putEdge(0, 1, nextWeight(0, 1));
        }
        if (numberOfVertices == 2) {
            return;
        }
        if (numberOfVertices > 2) {
            final int lastIndex = numberOfVertices - 1;
            final int preLastIndex = lastIndex - 1;
            if (! mainVerticesTree.contains(lastIndex)) {
                putEdge(preLastIndex, lastIndex, nextWeight(preLastIndex, lastIndex));
            }
            for (int i = 1; i < lastIndex; i++) {
                if (! mainVerticesTree.contains(i)) {
                    putEdge(i, i + 1, nextWeight(i, i + 1));
                }
            }
        }
    }

}
