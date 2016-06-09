package ru.ifmo.kot.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.protocol.ResponseStatus;

import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

/**
 * Created on 08.06.16.
 */
public class ServerSendMessageTask implements Runnable {

    private static final Logger LOGGER = LogManager.getFormatterLogger(ServerSendMessageTask.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<String, ResponseStatus> statusMap;
    private final List<Session> addressees;
    private final Consumer<Session> messageSending;

    public ServerSendMessageTask(final List<Session> addressees, final ConcurrentMap<String, ResponseStatus>
            statusMap, final Consumer<Session> messageSending) {
        this.statusMap = statusMap;
        this.addressees = addressees;
        this.messageSending = messageSending;
    }

    private boolean checkPass(final String mapKey) {
        if (statusMap.containsKey(mapKey)) {
            final ResponseStatus status = statusMap.get(mapKey);
            return status.equals(ResponseStatus.PASS);
        } else {
            return false;
        }
    }

    @Override
    public void run() {
        IntStream.range(0, addressees.size()).forEach(i -> {
            final Session address = addressees.get(i);
            final String addressId = address.getId();
            if (!checkPass(addressId)) {
                final Future<Void> future = executor.submit(
                        new WaitingForResponseTask(addressId,
                                (key) -> {
                                    if (statusMap.containsKey(key)) {
                                        final ResponseStatus status = statusMap.get(key);
                                        switch (status) {
                                            case OK:
                                            case FAIL:
                                                return true;
                                            case NOT_ACCEPTED:
                                                return false;
                                            default:
                                                return false;
                                        }
                                    } else {
                                        return false;
                                    }
                                }));
                messageSending.accept(address);
                try {
                    future.get(20, TimeUnit.SECONDS);
                } catch (final InterruptedException | ExecutionException e) {
                    LOGGER.error("Internal server error");
                } catch (final TimeoutException e) {
                    LOGGER.error("The player #%d does not respond", (i + 1));
                }
            }
        });
    }
}
