package ru.ifmo.kot.game.aibase;

import java.text.MessageFormat;
import java.util.Random;

/**
 Created on 26.05.16.
 */
public interface Ai {

	static String generateName() {
		return MessageFormat.format("Sapsan{0}", new Random().nextInt(100));
	}

	String move();

	default String move(final String missedParameter) {
		return move();
	}

	default String name() {
		return "Sapsan";
	}

	default String name(final String missedParameter) {
		String name = generateName();
		while (name.equals(missedParameter)) {
			name = generateName();
		}
		return name;
	}

}
