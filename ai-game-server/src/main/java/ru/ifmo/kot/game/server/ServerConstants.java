package ru.ifmo.kot.game.server;

import ru.ifmo.kot.game.Engine;

import javax.json.JsonObject;

interface ServerConstants {
    String SETTINGS_KEY = "server";
    String PORT_KEY = "port";
    String CONTEXT_PATH_KEY = "contextPath";
    String NUM_OF_CLIENTS_KEY = "numOfClients";
    JsonObject SETTINGS = Engine.getSettings().getJsonObject(SETTINGS_KEY);
    int PORT = SETTINGS.getInt(PORT_KEY);
    String CONTEXT_PATH = SETTINGS.getString(CONTEXT_PATH_KEY);
    int NUM_OF_CLIENTS = SETTINGS.getInt(NUM_OF_CLIENTS_KEY);
}
