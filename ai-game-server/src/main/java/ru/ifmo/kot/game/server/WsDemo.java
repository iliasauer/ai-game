package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

@ServerEndpoint("/wsdemo")
public class WsDemo {

    private static final Logger LOGGER = LogManager.getFormatterLogger(WsDemo.class);

    private Queue<Session> clients = new ArrayBlockingQueue<>(2);

    @OnOpen
    public void addClient(final Session session) {
        clients.offer(session);
        LOGGER.debug("A new player was added successfully");
    }

    public void removeClient(final Session session) {
        clients.
    }


}
