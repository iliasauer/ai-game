package ru.ifmo.kot.game.model;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.util.BinaryRandom;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.ifmo.kot.game.model.Graph.CONTENT_KEY;
import static ru.ifmo.kot.game.model.Graph.EDGE;
import static ru.ifmo.kot.game.model.Graph.NAME_KEY;
import static ru.ifmo.kot.game.model.Graph.SOURCE_KEY;
import static ru.ifmo.kot.game.model.Graph.TARGET_KEY;
import static ru.ifmo.kot.game.model.Graph.TYPE_KEY;
import static ru.ifmo.kot.game.model.Graph.VERTEX;
import static ru.ifmo.kot.game.model.Graph.WEIGHT_KEY;
import static ru.ifmo.kot.game.model.ModelConstants.COEFFICIENT_OF_EDGE_CONTENT_NUMBER;
import static ru.ifmo.kot.game.model.ModelConstants.THRESHOLD_OF_BENEFIT;
import static ru.ifmo.kot.game.model.ModelConstants.THRESHOLD_OF_OBSTACLE;

public class SymbolGraph {

    private static final Logger LOGGER = LogManager.getFormatterLogger(SymbolGraph.class);
    private static final Random USUAL_RANDOM = new Random();
    private static final BinaryRandom BINARY_RANDOM =
        new BinaryRandom(COEFFICIENT_OF_EDGE_CONTENT_NUMBER);
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
//        graph.edges().forEach(edge -> {
//            final String vrtxName1 = name(edge.anyVertexIndex());
//            final String vrtxName2 = name(edge.otherVertexIndex());
//            final double edgeCoeff = edgeCoefficient(edge.weight());
//            if(BINARY_RANDOM.nextBoolean()) {
//                if(edgeCoeff < THRESHOLD_OF_BENEFIT) {
//                    putEdge(vrtxName1, vrtxName2, EdgeContent.BENEFIT);
//                    LOGGER.debug("On %s-%s benefit was added", vrtxName1, vrtxName2);
//                } else if (edgeCoeff > THRESHOLD_OF_OBSTACLE) {
//                    putEdge(vrtxName1, vrtxName2, EdgeContent.OBSTACLE);
//                    LOGGER.debug("On %s-%s obstacle was added", vrtxName1, vrtxName2);
//                }
//            }
//        });
    }

    private double edgeCoefficient(final double weight) {
        return weight / graph.maxWeight();
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

    public JsonArray graphAsJson() {
        final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
        verticesNames.forEach(
            vertexName -> jsonArrayBuilder.add(vertexAsJson(new JsonVertex(vertexName))));
        graph.edges().forEach(edge -> jsonArrayBuilder.add(edgeAsJson(
            new JsonEdge(name(edge.anyVertexIndex()), name(edge.otherVertexIndex()),
                edge.weight()
            ))));
        return jsonArrayBuilder.build();
    }
    //	public void printVertices() {
    //		for (int i = 0; i < verticesNames.size(); i++) {
    //			System.out.println(i + ": " + verticesNames.get(i));
    //		}
    //	}
    //
    //	public void printEdges() {
    //		for (final Edge edge : graph.edges()) {
    //			final int srcVrtxIndx = edge.anyVertexIndex();
    //			final int dstVrtxIndx = edge.otherVertexIndex(srcVrtxIndx);
    //			System.out.println(MessageFormat.format(Edge.STRING_PATTERN,
    //					name(srcVrtxIndx),
    //					name(dstVrtxIndx),
    //					edge.weight()));
    //		}
    //	}
    //	public List<String> verticesNames() {
    //		return verticesNames;
    //	}

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

    private boolean putEdge(
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

    private JsonObject vertexAsJson(final JsonVertex vertex) {
        return Json.createObjectBuilder().add(TYPE_KEY, VERTEX).add(
            CONTENT_KEY, Json.createObjectBuilder().add(NAME_KEY, vertex.getId())).build();
    }

    private JsonObject edgeAsJson(final JsonEdge edge) {
        return Json.createObjectBuilder().add(TYPE_KEY, EDGE).add(
            CONTENT_KEY, Json.createObjectBuilder().add(NAME_KEY, edge.getId()).add(SOURCE_KEY,
                edge.getSource()
            ).add(TARGET_KEY, edge.getTarget()).add(
                WEIGHT_KEY, edge.getWeight())).build();
    }

    private static class JsonVertex {

        private final String id;

        JsonVertex(final String id) {
            this.id = id;
        }

        String getId() {
            return id;
        }
    }

    private static class JsonEdge {

        private final String id;
        private final String source;
        private final String target;
        private final int weight;

        JsonEdge(final String srcVrtxName, final String dstVrtxName, final int weight) {
            id = srcVrtxName + dstVrtxName;
            source = srcVrtxName;
            target = dstVrtxName;
            this.weight = weight;
        }

        String getId() {
            return id;
        }

        String getSource() {
            return source;
        }

        String getTarget() {
            return target;
        }

        int getWeight() {
            return weight;
        }
    }
}
