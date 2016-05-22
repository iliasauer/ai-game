package ru.ifmo.kot.tools;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

/**
 * Created on 22.05.16.
 */
public interface JsonCollectors {

	public static <T> Collector<Map.Entry<T, T>, ?, JsonObjectBuilder> toJsonBuilder() {
		return null;
	}

	static JsonObjectBuilder combine(final JsonObjectBuilder left, final JsonObjectBuilder
			right) {
		final JsonObjectBuilder result = Json.createObjectBuilder();
		final JsonObject leftObject = left.build();
		final JsonObject rightObject = right.build();
		leftObject.keySet().stream().forEach(key -> result.add(key, leftObject.get(key)));
		rightObject.keySet().stream().forEach(key -> result.add(key, rightObject.get(key)));
		return result;
	}

}
