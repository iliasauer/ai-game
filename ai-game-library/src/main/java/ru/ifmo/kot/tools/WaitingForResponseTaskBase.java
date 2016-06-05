package ru.ifmo.kot.tools;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 Created on 05.06.16.
 */
public abstract class WaitingForResponseTaskBase<T> implements Callable<T> {

	private final Map<String, T> statusMap;
	private final String id;
	private final Predicate<Map<String, T>> mapChecker;

	public WaitingForResponseTaskBase(final Map<String, T> statusMap, final String id,
		final Predicate<Map<String, T>> mapChecker) {
		this.statusMap = statusMap;
		this.id = id;
		this.mapChecker = mapChecker;
	}

	public String getId() {
		return id;
	}

	@Override
	public T call()
	throws Exception {
		while(mapChecker.test(statusMap)) {
			TimeUnit.MILLISECONDS.sleep(1L);
		}
		return null;
	}
}
