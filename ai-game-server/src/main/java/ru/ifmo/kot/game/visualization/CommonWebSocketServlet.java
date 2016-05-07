package ru.ifmo.kot.game.visualization;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CommonWebSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(CommonWebSocket.class);
        factory.getPolicy().setIdleTimeout(100000000); //TODO check it
    }

    private static class CommonWebSocket extends WebSocketAdapter {

        private static final Logger LOGGER = LogManager.getLogger(CommonWebSocket.class);

        private Session session;
        private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

        private class Task implements Runnable {
            @Override
            public synchronized void run() {
                sendMessage("");
            }
        }

        @Override
        public void onWebSocketConnect(Session session) {
            this.session = session;
            LOGGER.debug("The web socket connection was set successfully.");
        }

        @Override
        public void onWebSocketText(String message) {
            LOGGER.debug("The message received: " + message + ".");
            switch (message) {
                case "startLog":
                    executor.scheduleAtFixedRate(new Task(), 100, 50, TimeUnit
                            .MILLISECONDS);
                    break;
                case "stopLog":
                    executor.shutdown();
                    executor = Executors.newScheduledThreadPool(1);
                    break;
            }
        }

        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            LOGGER.debug("The web socket connection was closed on status " + statusCode +
                    ".\nReason: " + reason + ".");
        }

        @Override
        public void onWebSocketError(Throwable ignored) {
            LOGGER.error("The internal UI mode error");
        }

        private void sendMessage(String message) {
            try {
                if (session.isOpen()) {
                    session.getRemote().sendString(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void stop() {
            try {
                session.disconnect();
            } catch (IOException e) {
                LOGGER.debug("The WebSocket session is over.");
            }
        }
    }
}
