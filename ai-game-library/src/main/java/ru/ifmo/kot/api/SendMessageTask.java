package ru.ifmo.kot.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Session;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

/**
 Created on 09.06.16.
 */
public abstract class SendMessageTask<T> implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(SendMessageTask.class);

    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, T> statusMap;
    private final Consumer<Session> messageSender;

    public SendMessageTask(final Map<String, T> statusMap,
        final Consumer<Session> messageSender) {
        this.statusMap = statusMap;
        this.messageSender = messageSender;
    }

    protected void send(final Session address,
        final WaitingForResponseTask waitingTask) {
        final Future<Void> waiter = executor.submit(waitingTask);
        messageSender.accept(address);
        try {
            waiter.get(20, TimeUnit.SECONDS);
        } catch(final InterruptedException | ExecutionException e) {
            LOGGER.error("Internal server error");
        } catch(final TimeoutException e) {
            LOGGER.error("The addressee %s does not respond", address.getId());
        }
    }

    protected Map<String, T> statusMap() {
        return statusMap;
    }
}
