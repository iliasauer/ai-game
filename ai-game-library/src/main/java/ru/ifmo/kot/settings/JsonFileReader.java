package ru.ifmo.kot.settings;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created on 07.05.16.
 */
public interface JsonFileReader {

	Logger LOGGER = LogManager.getLogger(JsonFileReader.class);

	static JsonObject readJson(final String jsonFilePath) {
		final ClassLoader classloader = Thread.currentThread().getContextClassLoader();
		final InputStream fileAsInputStream = classloader.getResourceAsStream(jsonFilePath);
		return Json.createReader(new InputStreamReader(fileAsInputStream)).readObject();
	}
}