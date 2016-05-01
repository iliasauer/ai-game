package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.Game;
import ru.ifmo.kot.game.model.Graph;
import ru.ifmo.kot.game.util.JsonUtil;

import java.util.List;
import java.util.Map;

import static ru.ifmo.kot.game.elements.ElementsConstants.VERTICES_NAMES_KEY;
import static ru.ifmo.kot.game.elements.ElementsConstants.VERTICES_FILE_PATH_KEY;

public class Field {

    private static final Map<String, Object> SETTINGS =
            Game.getSettings(ElementsConstants.SETTINGS_KEY);
    private static final Logger LOGGER = LogManager.getLogger(Field.class);

    private static Object getSetting(final String key) {
        return SETTINGS.get(key);
    }

    private Graph gameModel;

    @SuppressWarnings("unchecked")
    public Field() {
        final Map<String, Object> verticesMap = JsonUtil.loadJsonFile((String) getSetting
                (VERTICES_FILE_PATH_KEY));
        List<String> verticesNames = null;
        if (verticesMap != null) {
            verticesNames = (List<String>) verticesMap.get(VERTICES_NAMES_KEY);
        } else {
            LOGGER.error("Vertices are not specified");
        }
        if (verticesNames != null) {
            gameModel = new Graph(verticesNames);
        } else {
            LOGGER.error("Vertices names are not specified");
        }
    }

    public Graph getGameModel() {
        return gameModel;
    }
}

