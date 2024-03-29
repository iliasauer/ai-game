package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.api.SendMessageTask;
import ru.ifmo.kot.api.WaitingForResponseTask;
import ru.ifmo.kot.protocol.ResponseStatus;

import javax.websocket.Session;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 Created on 09.06.16.
 */
class SendInvitesTask extends SendMessageTask<ResponseStatus> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(SendInvitesTask.class);

    private final List<Session> addressees;

    SendInvitesTask(
        final Map<String, ResponseStatus> statusMap, final Consumer<Session> messageSender,
        final List<Session> addressees
    ) {
        super(statusMap, messageSender);
        this.addressees = addressees;
    }

    private boolean checkMapKey(
        final String mapKey, final Function<ResponseStatus, Boolean> trueAction
    ) {
        if(statusMap().containsKey(mapKey)) {
            final ResponseStatus status = statusMap().get(mapKey);
            return trueAction.apply(status);
        } else {
            return false;
        }
    }

    @Override
    public Void call() throws Exception {
        addressees.forEach((addressee) -> {
            final String addresseeId = addressee.getId();
            if (!checkMapKey(addresseeId, status -> status.equals(ResponseStatus.PASS))) {
                final BooleanSupplier moveEnd = () -> checkMapKey(addresseeId, status -> {
                    switch(status) {
                        case OK:
                        case FAIL:
                            return true;
                        default:
                            return false;
                    }
                });
                try {
                    send(addressee, new WaitingForResponseTask(moveEnd));
                } catch(final InterruptedException | ExecutionException e) {
                    LOGGER.info("Internal server error");
                } catch(final TimeoutException e) {
                    LOGGER.info("Timeout error");
                }
            }

        });
        return null;
    }
}
