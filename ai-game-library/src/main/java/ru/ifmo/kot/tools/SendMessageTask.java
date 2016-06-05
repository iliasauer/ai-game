package ru.ifmo.kot.tools;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 Created on 05.06.16.
 */
public class SendMessageTask<T>
	implements Runnable {

	private final List<Session> addressees;
	private final Map<String, T> statusMap;
	private final Command command;
	private final IntConsumer addresseeAction;
	private final Consumer<Map<String, T>> afterAction;

	public SendMessageTask(final List<Session> addressees,
		final Map<String, T> statusMap ,final Command command,
		final IntConsumer addresseeAction, final Consumer<Map<String, T>> afterAction) {
		this.command = command;
		this.addressees = addressees;
		this.statusMap = statusMap;
		this.addresseeAction = addresseeAction;
		this.afterAction = afterAction;
	}

	public Command getCommand() {
		return command;
	}

	public Map<String, T> getStatusMap() {
		return statusMap;
	}

	public List<Session> getAddressees() {
		return addressees;
	}

	@Override
	public void run() {
		IntStream.range(0, addressees.size()).forEach(addresseeAction);
		afterAction.accept(statusMap);
	}
}
