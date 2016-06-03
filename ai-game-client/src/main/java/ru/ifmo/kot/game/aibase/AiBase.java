package ru.ifmo.kot.game.aibase;

import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.api.ServerApiImpl;

/**
 Created on 29.05.16.
 */
public abstract class AiBase implements Ai {

	private final ServerApi api = new ServerApiImpl();

	protected ServerApi api() {
		return api;
	}
}
