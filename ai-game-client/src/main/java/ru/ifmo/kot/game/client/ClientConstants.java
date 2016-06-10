package ru.ifmo.kot.game.client;

import ru.ifmo.kot.game.Engine;

import javax.json.JsonObject;

interface ClientConstants {
    String SETTINGS_KEY = "client";
    String SERVER_URL_KEY = "serverUrl";
    JsonObject SETTINGS = Engine.getSettings().getJsonObject(SETTINGS_KEY);
    String SERVER_URL = SETTINGS.getString(SERVER_URL_KEY);
}
