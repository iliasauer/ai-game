package ru.ifmo.kot.game.elements;

import ru.ifmo.kot.game.Engine;

import javax.json.JsonObject;

interface ElementsConstants {
    String SETTINGS_KEY = "elements";
    String VERTICES_FILE_PATH_KEY = "verticesFilePath";
    String VERTICES_NAMES_KEY = "cities";
    String PLAYERS_LIVES_NUMBER_KEY = "numberOfLives";
    JsonObject SETTINGS = Engine.getSettings().getJsonObject(SETTINGS_KEY);
    String VERTICES_FILE_PATH = SETTINGS.getString(VERTICES_FILE_PATH_KEY);

    String GAME_MODEL_KEY = "map";
}
