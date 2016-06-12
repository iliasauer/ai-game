package ru.ifmo.kot.game.elements;

import org.junit.Before;
import org.junit.Test;
import ru.ifmo.kot.game.model.Edge;
import ru.ifmo.kot.game.model.SymbolGraph;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.stream.IntStream;

@SuppressWarnings("FieldCanBeLocal")
public class FieldTest {

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

    List<int[]> coordinates;
    SymbolGraph gameModel;

    @Before
    public void initFields() throws Exception {
        Field field = new Field();
        coordinates = field.getCoordinates();
        gameModel = field.getGameModel();
    }

    @Test
    public void shouldEncodeField() throws Exception {
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
                System.out.println(jsonStringWriter.toString());
            }
        } catch(final IOException e) {
        }
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
                        .write(TYPE_KEY, 1)
                        .write(WEIGHT_KEY, edge.weight())
                    .writeEnd()
                .writeEnd();
        });
    }

}