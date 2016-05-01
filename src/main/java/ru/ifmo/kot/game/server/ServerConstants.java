package ru.ifmo.kot.game.server;

import ru.ifmo.kot.game.Game;

import java.util.Map;

interface ServerConstants {
    String SETTINGS_KEY = "server";
    String PORT_KEY = "port";
    String CONTEXT_PATH_KEY = "contextPath";
    String WEBAPP_PATH = "src/main/webapp";
    String WEBXML_PATH = WEBAPP_PATH + "/WEB-INF/web.xml";
    Map<String, Object> SETTINGS = Game.getSettings(SETTINGS_KEY);
}
