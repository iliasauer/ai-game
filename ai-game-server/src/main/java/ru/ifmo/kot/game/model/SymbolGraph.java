package ru.ifmo.kot.game.model;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonString;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ru.ifmo.kot.game.model.Graph.*;

public class SymbolGraph {

	private Map<String, Integer> vertexIndices;
	private List<String> verticesNames;
	private Graph graph;

	public SymbolGraph(final List<String> verticesNames) {
		this.verticesNames = verticesNames;
		vertexIndices = new HashMap<>();
		for (int i = 0; i < verticesNames.size(); i++) {
			vertexIndices.put(verticesNames.get(i), i);
		}
		graph = new UndirectedWeightedGraph(verticesNames.size());
	}

	public boolean contains(final String vertexName) {
		return verticesNames.contains(vertexName);
	}

	public int index(final String vertexName) {
		return vertexIndices.get(vertexName);
	}

	public String name(final int index) {
		return verticesNames.get(index);
	}

	public Graph graph() {
		return graph;
	}

	public void printVertices() {
		for (int i = 0; i < verticesNames.size(); i++) {
			System.out.println(i + ": " + verticesNames.get(i));
		}
	}

	public void printEdges() {
		for (final Edge edge : graph.edges()) {
			final int srcVrtxIndx = edge.anyVertexIndex();
			final int dstVrtxIndx = edge.otherVertexIndex(srcVrtxIndx);
			System.out.println(MessageFormat.format(Edge.STRING_PATTERN,
					name(srcVrtxIndx),
					name(dstVrtxIndx),
					edge.weight()));
		}
	}

	public List<String> getVerticesNames() {
		return verticesNames;
	}

	public boolean putEdge(final String vertexName1, final String vertexName2, final int weight) {
		if (contains(vertexName1) && contains(vertexName2)) {
			graph.putEdge(index(vertexName1), index(vertexName2), weight);
			return true;
		} else {
			return false;
		}
	}

	public int getWeight(final String vertexName1, final String vertexName2) {
		if (contains(vertexName1) && contains(vertexName2)) {
			return graph.getWeight(index(vertexName1), index(vertexName2));
		} else {
			return -1;
		}
	}

	private JsonObject vertexAsJson(final JsonVertex vertex) {
		return Json.createObjectBuilder()
				.add(TYPE_KEY, VERTEX)
				.add(CONTENT_KEY, Json.createObjectBuilder()
						.add(NAME_KEY, vertex.getId()))
				.build();
	}

	private JsonObject edgeAsJson(final JsonEdge edge) {
		return Json.createObjectBuilder()
				.add(TYPE_KEY, EDGE)
				.add(CONTENT_KEY, Json.createObjectBuilder()
						.add(NAME_KEY, edge.getId())
						.add(SOURCE_KEY, edge.getSource())
						.add(TARGET_KEY, edge.getTarget()))
				.build();
	}

	public JsonArray graphAsJson() {
		final JsonArrayBuilder jsonArrayBuilder = Json.createArrayBuilder();
		verticesNames.forEach(vertexName -> jsonArrayBuilder.add(
				vertexAsJson(
						new JsonVertex(vertexName))));
		graph.edges().forEach(edge -> jsonArrayBuilder.add(
				edgeAsJson(
						new JsonEdge(
								name(edge.anyVertexIndex()),
								name(edge.otherVertexIndex(edge.anyVertexIndex()))))));
		return jsonArrayBuilder.build();
	}

	private static class JsonVertex {
		private final String id;

		JsonVertex(final String id) {
			this.id = id;
		}

		String getId() {
			return id;
		}
	}

	private static class JsonEdge {
		private final String id;
		private final String source;
		private final String target;

		JsonEdge(final String srcVrtxName, final String dstVrtxName) {
			id = srcVrtxName + dstVrtxName;
			source = srcVrtxName;
			target = dstVrtxName;
		}

		String getId() {
			return id;
		}

		String getSource() {
			return source;
		}

		public String getTarget() {
			return target;
		}
	}
}
