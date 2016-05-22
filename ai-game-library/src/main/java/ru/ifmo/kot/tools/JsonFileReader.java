package ru.ifmo.kot.tools;

import com.sun.istack.internal.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;

/**
 * Created on 07.05.16.
 */
public interface JsonFileReader {

	Logger LOGGER = LogManager.getLogger(JsonFileReader.class);

	@Nullable
	static JsonObject readJson(final String jsonFilePath) {
		final ClassLoader classLoader = JsonFileReader.class.getClassLoader();
		final URL jsonFileUrl = classLoader.getResource(jsonFilePath);
		if (jsonFileUrl != null) {
			try {
				File jsonFile = new File(URLDecoder.decode(jsonFileUrl.getFile(), "UTF-8"));
				return Json.createReader(new FileReader(jsonFile)).readObject();
			} catch (UnsupportedEncodingException e) {
				LOGGER.error("The path is not correct");
			} catch (FileNotFoundException e) {
				LOGGER.error("The file is not found");
			}
		}
		return null;
	}
}