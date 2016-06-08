package ru.ifmo.kot.game;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.settings.JsonFileReader;

import javax.json.JsonObject;

public class Engine {

    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final String SETTINGS_FILE_PATH = "settings.json";
    private static JsonObject settings;

    static {
        loadSettingsFromFile();
    }

    private static void loadSettingsFromFile() {
        settings = JsonFileReader.readJson(SETTINGS_FILE_PATH);
        if (settings == null) {
            LOGGER.error("Settings has not loaded. Program is finishing.");
            System.exit(-1);
        }
    }


    public static JsonObject getSettings() {
        return settings;
    }
}
