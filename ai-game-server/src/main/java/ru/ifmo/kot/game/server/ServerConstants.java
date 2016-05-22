package ru.ifmo.kot.game.server;

import ru.ifmo.kot.game.Game;

import javax.json.JsonObject;
import java.util.Map;

interface ServerConstants {
    String SETTINGS_KEY = "server";
    String PORT_KEY = "port";
    String CONTEXT_PATH_KEY = "contextPath";
    String MAX_NUM_OF_CLIENTS_KEY = "maxNumOfClients";
    JsonObject SETTINGS = Game.getSettings().getJsonObject(SETTINGS_KEY);
    int PORT = SETTINGS.getInt(PORT_KEY);
    String CONTEXT_PATH = SETTINGS.getString(CONTEXT_PATH_KEY);
    int MAX_NUM_OF_CLIENTS = SETTINGS.getInt(MAX_NUM_OF_CLIENTS_KEY);
}
