package ru.ifmo.kot.game.client;

import ru.ifmo.kot.api.SendMessageTask;
import ru.ifmo.kot.api.WaitingForResponseTask;

import javax.websocket.Session;
import java.util.Map;
import java.util.function.Consumer;

/**
 Created on 09.06.16.
 */
class SendApiMessageTask<T> extends SendMessageTask<T> {

    private final Session addressee;
    private final String mapKey;

    SendApiMessageTask(
        final Map<String, T> statusMap, final Consumer<Session> messageSender,
        final Session addressee, final String mapKey
    ) {
        super(statusMap, messageSender);
        this.addressee = addressee;
        this.mapKey = mapKey;
    }

    @Override
    public Void call() throws Exception {
        statusMap().remove(mapKey);
        send(addressee, new WaitingForResponseTask(() -> statusMap().containsKey(mapKey)));
        return null;
    }
}
