package ru.ifmo.kot.game.model;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SymbolGraph {

    private Map<String, Integer> vertexIndices;
    private List<String> verticesNames;
    private Graph graph;

    public SymbolGraph(final List<String> verticesNames) {
        this.verticesNames = verticesNames;
        vertexIndices = new HashMap<>();
        for (int i = 0; i < verticesNames.size(); i++) {
            vertexIndices.put(verticesNames.get(i), i);
        }
        graph = new UndirectedWeightedGraph(verticesNames.size());
    }

    public boolean contains(final String vertexName) {
        return verticesNames.contains(vertexName);
    }

    public int index(final String vertexName) {
        return vertexIndices.get(vertexName);
    }

    public String name(final int index) {
        return verticesNames.get(index);
    }

    public Graph graph() {
        return graph;
    }

    public void printVertices() {
        for (int i = 0; i < verticesNames.size(); i++) {
            System.out.println(i + ": " + verticesNames.get(i));
        }
    }

    public void printEdges() {
        for (final Edge edge: graph.edges()) {
            final int srcVrtxIndx = edge.anyVertexIndex();
            final int dstVrtxIndx = edge.otherVertexIndex(srcVrtxIndx);
            System.out.println(MessageFormat.format(Edge.STRING_PATTERN,
                    name(srcVrtxIndx),
                    name(dstVrtxIndx),
                    edge.weight()));
        }
    }

    public boolean putEdge(final String vertexName1, final String vertexName2, final int weight) {
        if (contains(vertexName1) && contains(vertexName2)) {
            graph.putEdge(index(vertexName1), index(vertexName2), weight);
            return true;
        } else {
            return false;
        }
    }

    public int getWeight(final String vertexName1, final String vertexName2) {
        if (contains(vertexName1) && contains(vertexName2)) {
            return graph.getWeight(index(vertexName1), index(vertexName2));
        } else {
            return -1;
        }
    }
}
