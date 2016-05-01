package ru.ifmo.kot.game.elements;

import ru.ifmo.kot.game.Game;

import java.util.Map;

interface ElementsConstants {
    String SETTINGS_KEY = "elements";
    String VERTICES_FILE_PATH_KEY = "verticesFilePath";
    String VERTICES_NAMES_KEY = "cities";
    String PLAYERS_LIVES_NUMBER_KEY = "numberOfLives";
    Map<String, Object> SETTINGS = Game.getSettings(ElementsConstants.SETTINGS_KEY);
}
