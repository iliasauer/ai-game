package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.model.Edge;
import ru.ifmo.kot.game.model.EdgeContent;
import ru.ifmo.kot.game.model.SymbolGraph;
import ru.ifmo.kot.game.util.BinaryRandom;
import ru.ifmo.kot.game.util.RandomUtil;
import ru.ifmo.kot.settings.JsonFileReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.lang.Double.sum;
import static java.lang.Math.pow;
import static java.lang.Math.sqrt;
import static ru.ifmo.kot.game.elements.ElementsConstants.COEFFICIENT_OF_EDGE_CONTENT_NUMBER;
import static ru.ifmo.kot.game.elements.ElementsConstants.GRID_STEP;
import static ru.ifmo.kot.game.elements.ElementsConstants.OBSTACLE_MAX_FACTOR;
import static ru.ifmo.kot.game.elements.ElementsConstants.THRESHOLD_OF_BENEFIT;
import static ru.ifmo.kot.game.elements.ElementsConstants.THRESHOLD_OF_OBSTACLE;
import static ru.ifmo.kot.game.elements.ElementsConstants.VERTICES_FILE_PATH;
import static ru.ifmo.kot.game.elements.ElementsConstants.VERTICES_NAMES_KEY;

public class Field {

    private static final String FIELD_KEY = "elements";
    private static final String VERTICES_KEY = "nodes";
    private static final String EDGES_KEY = "edges";
    private static final String CONTENT_KEY = "data";
    private static final String ID_KEY = "id";
    private static final String NAME_KEY = "name";
    private static final String SOURCE_KEY = "source";
    private static final String DESTINATION_KEY = "target";
    private static final String TYPE_KEY = "type";
    private static final String WEIGHT_KEY = "weight";
    private static final String COORDINATES_KEY = "position";
    private static final String X_KEY = "x";
    private static final String Y_KEY = "y";

    private static final Logger LOGGER = LogManager.getFormatterLogger(Field.class);
    private static Random USUAL_RANDOM = new Random();
    private static final BinaryRandom BINARY_RANDOM =
        new BinaryRandom(COEFFICIENT_OF_EDGE_CONTENT_NUMBER);
    private static final double MIN_WEIGHT_FACTOR = 1.1;
    private List<int[]> coordinates;
    private SymbolGraph gameModel;
    private static String[] startVertices;

    @SuppressWarnings("unchecked")
    public Field() {
        final JsonObject verticesJson =
                JsonFileReader.readJson(VERTICES_FILE_PATH);
        JsonArray verticesNamesArray = null;
        if (verticesJson != null) {
            verticesNamesArray = verticesJson.getJsonArray(VERTICES_NAMES_KEY);
        } else {
            LOGGER.error("Vertices are not specified");
        }
        if (verticesNamesArray != null) {
            final List<String> verticesNames =
                    verticesNamesArray.getValuesAs(JsonString.class).stream()
                            .collect(Collectors.mapping(
                                    JsonString::getString, Collectors.toList()));
            final int numberOfVertices = verticesNames.size();
            coordinates = distributeVerticesCoordinates(numberOfVertices);
            gameModel = new SymbolGraph(verticesNames);
            connectVertices();
            arrangeEdgeContent();

        } else {
            LOGGER.error("Vertices names are not specified");
        }
        final List<String> startVerticesList = gameModel.randomVertexNamesPair();
        startVertices = new String[startVerticesList.size()];
        startVerticesList.toArray(startVertices);
        LOGGER.info("Players should go from %s to %s", startVertices[0], startVertices[1]);
    }

    public List<int[]> getCoordinates() {
        return coordinates;
    }

    private void connectVertices() {
        final int numberOfVertices = coordinates.size();
        for (int srcVrtxIndex = 0; srcVrtxIndex < numberOfVertices - 1; srcVrtxIndex++) {
            final int QUEUE_MAX_SIZE = 5;
            final Queue<Map.Entry<Integer, Integer>> dstWeights =
                new PriorityQueue<>(QUEUE_MAX_SIZE, DstVrtxWeightPair.reversedComparator());
            for (int dstVrtxIndx = srcVrtxIndex + 1; dstVrtxIndx < numberOfVertices; dstVrtxIndx++) {
                final int[] srcVrtxCoords = coordinates.get(srcVrtxIndex);
                final int[] dstVrtxCoords = coordinates.get(dstVrtxIndx);
                final int weight = calculateWeight(srcVrtxCoords, dstVrtxCoords);
                dstWeights.offer(new DstVrtxWeightPair(dstVrtxIndx, weight));
                if(dstWeights.size() > QUEUE_MAX_SIZE) {
                    dstWeights.poll();
                }
            }
            final int currentNumberOfEdges = gameModel.graph().nextVertices(srcVrtxIndex).size();
            final int numberOfEdges = USUAL_RANDOM.nextInt(2) + 2 - currentNumberOfEdges;
            final Map.Entry<Integer, Integer>[] dstWeightArr = new DstVrtxWeightPair[dstWeights.size()];
            dstWeights.toArray(dstWeightArr);
            for (int i = 0; i < numberOfEdges; i++) {
                final Map.Entry<Integer, Integer> dstVrtxWeightPair = dstWeightArr[i];
                gameModel.graph().putEdge(srcVrtxIndex, dstVrtxWeightPair.getKey(), dstVrtxWeightPair.getValue());
            }
        }
    }

    private void arrangeEdgeContent() {
        gameModel.graph().edges().forEach(edge -> {
            final String vrtxName1 = gameModel.name(edge.anyVertexIndex());
            final String vrtxName2 = gameModel.name(edge.otherVertexIndex());
            final double edgeCoeff = edgeCoefficient(edge.weight());
            if(BINARY_RANDOM.nextBoolean()) {
                if(edgeCoeff < THRESHOLD_OF_BENEFIT) {
                    gameModel.putEdge(vrtxName1, vrtxName2, EdgeContent.BENEFIT);
                    LOGGER.debug("On %s-%s benefit was added", vrtxName1, vrtxName2);
                } else if (edgeCoeff > THRESHOLD_OF_OBSTACLE) {
                    gameModel.putEdge(vrtxName1, vrtxName2, EdgeContent.OBSTACLE);
                    LOGGER.debug("On %s-%s obstacle was added", vrtxName1, vrtxName2);
                }
            }
        });
    }

    private double edgeCoefficient(final double weight) {
        return weight / gameModel.graph().maxWeight();
    }

    private List<int[]> distributeVerticesCoordinates(final int numberOfVertices) {
        return distributeVerticesCoordinates(numberOfVertices, GRID_STEP);
    }

    private List<int[]> distributeVerticesCoordinates(final int verticesNum, final int step) {
        if(verticesNum < 0) {
            throw new IllegalArgumentException();
        }
        if(verticesNum == 0) {
            return Collections.emptyList();
        }
        final int numSqrt = ((int) (sqrt(verticesNum))) + 3;
        final int rowsNum = Math.max(2, numSqrt - 1);
        final int colsNum = numSqrt + 2;
        final int[][][] grid = new int[rowsNum][colsNum][2];
        final List<int[]> verticesCoordinates = new ArrayList<>();
        for(int i = 0; i < rowsNum; i++) {
            for(int j = 0; j < colsNum; j++) {
                final int[] coords = grid[i][j];
                if(USUAL_RANDOM.nextBoolean()) {
                    coords[0] = j * step + USUAL_RANDOM.nextInt(step);
                    coords[1] = -(i * step + USUAL_RANDOM.nextInt(step));
                    verticesCoordinates.add(coords);
                    if (verticesCoordinates.size() == verticesNum) {
                        return verticesCoordinates;
                    }
                } else {
                    coords[0] = -1;
                }
            }
        }
        return distributeVerticesCoordinates(verticesNum, step);
    }

    private int calculateWeight(final int[] coordinates1, final int[] coordinates2) {
        final int x = coordinates2[0] - coordinates1[0];
        final int y = coordinates2[1] - coordinates1[1];
        return (int) sqrt(sum(pow(x, 2), pow(y, 2)));
    }

    public String asJson() {
        try(
            final Writer jsonStringWriter = new StringWriter();
        ) {
            try(
                final JsonGenerator jsonGenerator = Json.createGenerator(jsonStringWriter);
            ) {
                jsonGenerator
                    .writeStartObject()
                    .writeStartObject(FIELD_KEY)
                    .writeStartArray(VERTICES_KEY);
                writeVertices(jsonGenerator);
                jsonGenerator.writeEnd()
                    .writeStartArray(EDGES_KEY);
                writeEdges(jsonGenerator);
                jsonGenerator.writeEnd()
                    .writeEnd()
                    .writeEnd();
                jsonGenerator.flush();
                return jsonStringWriter.toString();
            }
        } catch(final IOException e) {
            LOGGER.error("Internal server error");
        }
        return null;
    }

    private void writeVertices(final JsonGenerator jsonGenerator) {
        IntStream.range(0, coordinates.size()).forEach(i -> {
            final String name = gameModel.name(i);
            final int[] coords = coordinates.get(i);
            jsonGenerator
                .writeStartObject()
                .writeStartObject(CONTENT_KEY)
                .write(ID_KEY, "v" + i)
                .write(NAME_KEY, name)
                .writeEnd()
                .writeStartObject(COORDINATES_KEY)
                .write(X_KEY, coords[0])
                .write(Y_KEY, coords[1])
                .writeEnd()
                .writeEnd();
        });
    }

    private void writeEdges(final JsonGenerator jsonGenerator) {
        final Iterable<Edge> edges = gameModel.graph().edges();
        edges.forEach(edge -> {
            final int srcVrtx = edge.anyVertexIndex();
            final int dstVrtx = edge.otherVertexIndex();
            jsonGenerator
                .writeStartObject()
                .writeStartObject(CONTENT_KEY)
                .write(ID_KEY, "v" + srcVrtx + "-" + "v" + dstVrtx)
                .write(SOURCE_KEY, "v" + srcVrtx)
                .write(DESTINATION_KEY, "v" + dstVrtx)
                .write(TYPE_KEY, 2)
                .write(WEIGHT_KEY, edge.weight())
                .writeEnd()
                .writeEnd();
        });
    }

    public SymbolGraph getGameModel() {
        return gameModel;
    }

    public String[] getStartVertices() {
        return startVertices;
    }

    public Set<String> getNextVertices(final String vertexName) {
        return gameModel.nextVertices(vertexName);
    }

    public boolean doesVertexExist(final String vertexName) {
        return gameModel.contains(vertexName);
    }

    public boolean doesEdgeExist(final String vertexName1, final String vertexName2) {
        return doesVertexExist(vertexName1) && doesVertexExist(vertexName2) &&
            gameModel.getWeight(vertexName1, vertexName2) > 0;
    }

    public void changeWeight(final String vertexName1, final String vertexName2) {
        final int weight = gameModel.getWeight(vertexName1, vertexName2);
        gameModel.putEdge(vertexName1, vertexName2, (int) (weight *
                RandomUtil.nextDouble(MIN_WEIGHT_FACTOR, OBSTACLE_MAX_FACTOR)));
    }

    private static class DstVrtxWeightPair
        implements Map.Entry<Integer, Integer> {

        private static Comparator<Map.Entry<Integer, Integer>> REVERSED_COMPARATOR =
            Map.Entry.<Integer, Integer>comparingByValue().reversed();
        private final int key;
        private final int value;

        DstVrtxWeightPair(final int key, final int value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public Integer getKey() {
            return key;
        }

        @Override
        public Integer getValue() {
            return value;
        }

        @Override
        public Integer setValue(final Integer value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String toString() {
            return key + ": " + value;
        }

        public static Comparator<Map.Entry<Integer, Integer>> reversedComparator() {
            return REVERSED_COMPARATOR;
        }
    }

}

