package ru.ifmo.kot.tools;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Created on 22.05.16.
 */
class PlainJsonConsumer implements Consumer<Map<String, String>> {

	private final JsonObjectBuilder jsonBuilder = Json.createObjectBuilder();

	@Override
	public void accept(Map<String, String> stringStringMap) {

	}

	public void combine(final PlainJsonConsumer other) {

	}

	public JsonObjectBuilder jsonObjectBuilder() {
		return null;
	}


}
