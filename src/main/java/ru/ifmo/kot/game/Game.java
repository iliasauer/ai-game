package ru.ifmo.kot.game;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Map;

public class Game {

    private static final Logger LOGGER = LogManager.getRootLogger();
    private static final String SETTINGS_FILE_PATH = "settings.json";
    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    private static Map<String, Object> settings;
    static {
        loadSettingsFromFile();
    }

    public static void loadSettingsFromFile() {
        final ClassLoader classLoader = Game.class.getClassLoader();
        final URL settingsUrl = classLoader.getResource(SETTINGS_FILE_PATH);
        File settingsFile = null;
        if (settingsUrl != null) {
            settingsFile = new File(settingsUrl.getFile());
        } else {
            LOGGER.error("Settings file is not found");
            System.exit(-1);
        }
        try {
           settings = JSON_MAPPER.readValue(settingsFile, new
                    TypeReference<Map<String, Object>>(){});
            LOGGER.debug("Settings are loaded successfully");
        } catch (IOException e) {
            LOGGER.error("Settings cannot be loaded", e);
            System.exit(-1);
        }
    }

    public static Map<String, Object> getSettings() {
        return settings;
    }
}
