package ru.ifmo.kot.tools;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;

/**
 * Created on 05.06.16.
 */
public class SendMessageTask<T>
        implements Runnable {

    private static final Logger LOGGER = LogManager.getFormatterLogger(SendMessageTask.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Map<String, T> statusMap;
    private String mapKey;
    private final List<Session> addressees;
    private final Consumer<Session> messageSending;

    public SendMessageTask(final List<Session> addressees, final Map<String, T> statusMap,
                           final String mapKey, final Consumer<Session> messageSending) {
        this.statusMap = statusMap;
        this.mapKey = mapKey;
        this.addressees = addressees;
        this.messageSending = messageSending;
    }

    @Override
    public void run() {
        IntStream.range(0, addressees.size()).forEach(i -> {
            final Session address = addressees.get(i);
            final String tempMapKey;
            if (Objects.isNull(mapKey)) {
                tempMapKey = address.getId();
            } else {
                tempMapKey = mapKey;
            }
            statusMap.remove(tempMapKey);
            LOGGER.info("Send some to #%d participator", (i + 1));
            final Future<Void> future = executor.submit(
                    new WaitingForResponseTask<T>(tempMapKey, statusMap::containsValue));
            messageSending.accept(address);
            try {
                future.get(20, TimeUnit.SECONDS);
            } catch (final InterruptedException | ExecutionException e) {
                LOGGER.error("Internal server error");
            } catch (final TimeoutException e) {
                LOGGER.error("Participator #%d does not respond", (i + 1));
            }

        });
    }
}
