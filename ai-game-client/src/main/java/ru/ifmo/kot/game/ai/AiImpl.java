package ru.ifmo.kot.game.ai;

import ru.ifmo.kot.game.aibase.AiBase;

import java.util.List;
import java.util.Random;

/**
 Created on 29.05.16.
 */
public class AiImpl extends AiBase {

	private static final Random USUAL_RANDOM = new Random();

	@Override
	public String move() {
		final String currentVertex = api().currentVertex();
		final List<String> nextVertices = api().nextVertices(currentVertex);
		return nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size()));
	}

}
