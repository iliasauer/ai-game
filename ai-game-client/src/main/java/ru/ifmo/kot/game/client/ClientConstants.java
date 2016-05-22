package ru.ifmo.kot.game.client;

import ru.ifmo.kot.game.Game;

import javax.json.JsonObject;
import java.util.Map;

public interface ClientConstants {
    String SETTINGS_KEY = "client";
    String SERVER_URL_KEY = "serverUrl";
    JsonObject SETTINGS = Game.getSettings().getJsonObject(SETTINGS_KEY);
    String SERVER_URL = SETTINGS.getString(SERVER_URL_KEY);
}
