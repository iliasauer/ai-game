package ru.ifmo.kot.game.client;

import ru.ifmo.kot.game.Game;

import java.util.Map;

public interface ClientConstants {
    String SETTINGS_KEY = "client";
    String SERVER_URL_KEY = "serverUrl";
    Map<String, Object> SETTINGS = Game.getSettings(SETTINGS_KEY);
    String SERVER_URL = (String) SETTINGS.get(SERVER_URL_KEY);
}
