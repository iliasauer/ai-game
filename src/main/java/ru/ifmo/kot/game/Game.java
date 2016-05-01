package ru.ifmo.kot.game;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.util.JsonUtil;

import java.util.Map;

public class Game {

    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final String SETTINGS_FILE_PATH = "settings.json";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();
    private static Map<String, Object> settings;

    static {
        loadSettingsFromFile();
    }

    private static void loadSettingsFromFile() {
        settings = JsonUtil.loadJsonFile(SETTINGS_FILE_PATH);
        if (settings == null) {
            LOGGER.error("Settings has not loaded. Program is finishing.");
            System.exit(-1);
        }
    }


    @SuppressWarnings("unchecked")
    public static Map<String, Object> getSettings(final String key) {
        return (Map<String, Object>) settings.get(key);
    }
}
