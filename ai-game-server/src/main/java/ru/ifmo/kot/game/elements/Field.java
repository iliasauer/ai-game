package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.model.SymbolGraph;
import ru.ifmo.kot.tools.JsonFileMapper;

import java.util.List;
import java.util.Map;

import static ru.ifmo.kot.game.elements.ElementsConstants.*;

public class Field {

    private static final Logger LOGGER = LogManager.getLogger(Field.class);

    private SymbolGraph gameModel;

    @SuppressWarnings("unchecked")
    public Field() {
        final Map<String, Object> verticesMap =
                JsonFileMapper.readJson(VERTICES_FILE_PATH);
        List<String> verticesNames = null;
        if (verticesMap != null) {
            verticesNames = (List<String>) verticesMap.get(VERTICES_NAMES_KEY);
        } else {
            LOGGER.error("Vertices are not specified");
        }
        if (verticesNames != null) {
            gameModel = new SymbolGraph(verticesNames);
        } else {
            LOGGER.error("Vertices names are not specified");
        }
    }

    public SymbolGraph getGameModel() {
        return gameModel;
    }
}

