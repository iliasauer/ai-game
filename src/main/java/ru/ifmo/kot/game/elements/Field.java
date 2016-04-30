package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.Game;
import ru.ifmo.kot.game.model.Graph;
import ru.ifmo.kot.game.model.Vertex;
import ru.ifmo.kot.game.util.JsonUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static ru.ifmo.kot.game.elements.ElementsConstants.CITIES_VERTICES_KEY;
import static ru.ifmo.kot.game.elements.ElementsConstants.VERTICES_FILE_PATH_KEY;

public class Field {

    private static final Map<String, Object> SETTINGS =
            Game.getSettings(ElementsConstants.SETTINGS_KEY);
    private static final Logger LOGGER = LogManager.getLogger(Field.class);

    private static Object getSetting(final String key) {
        return SETTINGS.get(key);
    }

    private Graph gameModel;
    private List<Vertex> vertices;

    @SuppressWarnings("unchecked")
    public Field() {
        final Map<String, Object> verticesMap = JsonUtil.loadJsonFile((String) getSetting
                (VERTICES_FILE_PATH_KEY));
        List<String> cities = null;
        if (verticesMap != null) {
             cities = (List<String>) verticesMap.get(CITIES_VERTICES_KEY);
        } else {
            LOGGER.error("Vertices are not specified");
        }
        if (cities != null) {
            vertices = new ArrayList<>(cities.size());
            int index = 0;
            for (final String city: cities) {
                vertices.add(new Vertex(index++, city));
            }
        }
        gameModel = new Graph(vertices.size());
    }

    public Graph getGameModel() {
        return gameModel;
    }

    public List<Vertex> getVertices() {
        return vertices;
    }

    public void printVertices() {
        for (final Vertex vertex: vertices) {
            System.out.println(vertex);
        }
    }
}

