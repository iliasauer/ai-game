package ru.ifmo.kot.game.elements;

import ru.ifmo.kot.game.Engine;

import javax.json.JsonObject;

interface ElementsConstants {
    String SETTINGS_KEY = "elements";
    String VERTICES_FILE_PATH_KEY = "verticesFilePath";
    String VERTICES_NAMES_KEY = "cities";
    String GRID_STEP_KEY = "gridStep";
    String PLAYERS_LIVES_NUMBER_KEY = "numberOfLives";
    String TURN_POINTS_KEY = "turnPoints";
    String OBSTACLE_MAX_FACTOR_KEY = "obstacleMaxFactor";
    String BENEFIT_MAX_FACTOR_KEY = "benefitMaxFactor";
    JsonObject SETTINGS = Engine.getSettings().getJsonObject(SETTINGS_KEY);
    String VERTICES_FILE_PATH = SETTINGS.getString(VERTICES_FILE_PATH_KEY);
    int GRID_STEP = SETTINGS.getInt(GRID_STEP_KEY);
    int PLAYERS_LIVES_NUMBER = SETTINGS.getInt(PLAYERS_LIVES_NUMBER_KEY);
    int TURN_POINTS = SETTINGS.getInt(TURN_POINTS_KEY);
    double OBSTACLE_MAX_FACTOR =
            SETTINGS.getJsonNumber(OBSTACLE_MAX_FACTOR_KEY).doubleValue();
    double BENEFIT_MAX_FACTOR =
            SETTINGS.getJsonNumber(BENEFIT_MAX_FACTOR_KEY).doubleValue();

    String GAME_MODEL_KEY = "map";
}
