package ru.ifmo.kot.game.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Map;

public class JsonUtil {

    private static final Logger LOGGER = LogManager.getLogger(JsonUtil.class);

    private static final ObjectMapper JSON_MAPPER = new ObjectMapper();

    public static Map<String, Object> loadJsonFile(final String jsonFilePath) {
        final ClassLoader classLoader = JsonUtil.class.getClassLoader();
        final URL jsonFileUrl = classLoader.getResource(jsonFilePath);
        File jsonFile = null;
        if (jsonFileUrl != null) {
            try {
                jsonFile = new File(URLDecoder.decode(jsonFileUrl.getFile(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                LOGGER.error("The path is not correct");
            }
        } else {
            LOGGER.error("The file is not found");
            return null;
        }
        try {
            return JSON_MAPPER.readValue(jsonFile, new
                    TypeReference<Map<String, Object>>(){});
        } catch (IOException e) {
            LOGGER.error("The file cannot be mapped");
            return null;
        }
    }

}
