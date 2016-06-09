package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.api.WaitingForResponseTask;
import ru.ifmo.kot.protocol.ResponseStatus;

import javax.websocket.Session;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 Created on 08.06.16.
 */
class SendInviteTask
    implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(SendInviteTask.class);
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final ConcurrentMap<String, ResponseStatus> statusMap;
    private final List<Session> addressees;
    private final Consumer<Session> messageSending;

    SendInviteTask(
        final List<Session> addressees, final ConcurrentMap<String, ResponseStatus> statusMap,
        final Consumer<Session> messageSending
    ) {
        this.statusMap = statusMap;
        this.addressees = addressees;
        this.messageSending = messageSending;
    }

    private boolean checkMapKey(
        final String mapKey, final Function<ResponseStatus, Boolean> trueAction
    ) {
        if(statusMap.containsKey(mapKey)) {
            final ResponseStatus status = statusMap.get(mapKey);
            return trueAction.apply(status);
        } else {
            return false;
        }
    }

    @Override
    public Void call() {
        final int numberOfAddresses = addressees.size();
        IntStream.range(0, numberOfAddresses).forEach(i -> {
            final Session address = addressees.get(i);
            final String addressId = address.getId();
            if(!checkMapKey(addressId, status -> status.equals(ResponseStatus.PASS))) {
                final Future<Void> future = executor.submit(
                    new WaitingForResponseTask(() -> checkMapKey(addressId, status -> {
                        switch(status) {
                            case OK:
                            case FAIL:
                                return true;
                            case NOT_ACCEPTED:
                                return false;
                            default:
                                return false;
                        }
                    })));
                messageSending.accept(address);
                try {
                    future.get(20, TimeUnit.SECONDS);
                } catch(final InterruptedException | ExecutionException e) {
                    LOGGER.error("Internal server error");
                } catch(final TimeoutException e) {
                    LOGGER.error("The player %s does not respond", addressId);
                }
            }
        });
        return null;
    }
}
