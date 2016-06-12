package ru.ifmo.kot.game.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class SymbolGraph {

    private static final Random USUAL_RANDOM = new Random();
    private Map<String, Map<String, EdgeContent>> adjacencyEdgeMap;
    private Map<String, Integer> vertexIndices;
    private List<String> verticesNames;
    private Graph graph;

    public SymbolGraph(final List<String> verticesNames) {
        this.verticesNames = verticesNames;
        vertexIndices = new HashMap<>();
        for(int i = 0; i < verticesNames.size(); i++) {
            vertexIndices.put(verticesNames.get(i), i);
        }
        graph = new UndirectedWeightedGraph(verticesNames.size());
        adjacencyEdgeMap = new HashMap<>();
        verticesNames.forEach(vertexName -> adjacencyEdgeMap.put(vertexName, new HashMap<>()));
    }

    public boolean contains(final String vertexName) {
        return verticesNames.contains(vertexName);
    }

    private int index(final String vertexName) {
        return vertexIndices.get(vertexName);
    }

    public String name(final int index) {
        return verticesNames.get(index);
    }

    public Graph graph() {
        return graph;
    }

    public EdgeContent takeEdgeContent(final String vertexName1, final String vertexName2) {
        if(adjacencyEdgeMap.containsKey(vertexName1)) {
            final Map<String, EdgeContent> nextVerticesContents = adjacencyEdgeMap.get(vertexName1);
            if(nextVerticesContents.containsKey(vertexName2)) {
                final EdgeContent edgeContent = nextVerticesContents.get(vertexName2);
                nextVerticesContents.remove(vertexName2);
                return edgeContent;
            }
        }
        return null;
    }

    public int getWeight(final String vertexName1, final String vertexName2) {
        return graph.getWeight(index(vertexName1), index(vertexName2));
    }

    public boolean putEdge(
        final String vertexName1, final String vertexName2, final EdgeContent content
    ) {
        if(contains(vertexName1) && contains(vertexName2)) {
            putOneSideEdge(vertexName1, vertexName2, content);
            putOneSideEdge(vertexName2, vertexName1, content);
            return true;
        } else {
            return false;
        }
    }

    private void putOneSideEdge(
        final String vertexName1, final String vertexName2, final EdgeContent content
    ) {
        adjacencyEdgeMap.get(vertexName1).put(vertexName2, content);
    }

    public boolean putEdge(final String vertexName1, final String vertexName2, final int weight) {
        if(contains(vertexName1) && contains(vertexName2)) {
            graph.putEdge(index(vertexName1), index(vertexName2), weight);
            return true;
        } else {
            return false;
        }
    }

    private int randomVertexIndex() {
        return USUAL_RANDOM.nextInt(graph.numberOfVertices());
    }

    private int otherRandomVertex(int vertexIndex) {
        int otherVertexIndex = randomVertexIndex();
        while(vertexIndex == otherVertexIndex) {
            otherVertexIndex = randomVertexIndex();
        }
        return otherVertexIndex;
    }

    public Set<String> nextVertices(final String vrtxName) {
        return graph.nextVertices(index(vrtxName)).stream().collect(
            Collectors.mapping(this :: name, Collectors.toCollection(HashSet::new)));
    }

    private int[] randomVertexIndicesPair() {
        final int vertexIndex = randomVertexIndex();
        return new int[] {vertexIndex, otherRandomVertex(vertexIndex)};
    }

    public List<String> randomVertexNamesPair() {
        List<String> names = new ArrayList<>();
        for(int index : randomVertexIndicesPair()) {
            names.add(name(index));
        }
        return names;
    }

}
