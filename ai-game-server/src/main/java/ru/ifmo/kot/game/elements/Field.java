package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.model.SymbolGraph;
import ru.ifmo.kot.tools.JsonFileReader;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.ifmo.kot.game.elements.ElementsConstants.*;

public class Field {

    private static final Logger LOGGER = LogManager.getFormatterLogger(Field.class);

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
            gameModel = new SymbolGraph(verticesNames);
        } else {
            LOGGER.error("Vertices names are not specified");
        }
        final List<String> startVerticesList = gameModel.randomVertexNamesPair();
        startVertices = new String[startVerticesList.size()];
        startVerticesList.toArray(startVertices);
        LOGGER.info("Players should go from %s to %s", startVertices[0], startVertices[1]);
    }

    public SymbolGraph getGameModel() {
        return gameModel;
    }

    public JsonObject getGameModelAsJson() {
        return Json.createObjectBuilder().add(GAME_MODEL_KEY, gameModel.graphAsJson()).build();
    }

    public String[] getStartVertices() {
        return startVertices;
    }

    public Set<String> getNextVertices(final String vertexName) {
        return gameModel.nextVertices(vertexName);
    }

}

