package ru.ifmo.kot.game.server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

import java.util.concurrent.TimeUnit;

public class MainServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(MainSocket.class);
        factory.getPolicy().setIdleTimeout(TimeUnit.HOURS.toMillis(10));
    }

    private static class MainSocket extends WebSocketAdapter {

        private static final Logger LOGGER = LogManager.getFormatterLogger(MainSocket.class);

        private Session session;

        @Override
        public void onWebSocketConnect(final Session session) {
            this.session = session;
            LOGGER.debug("The websocket connection was set successfully");
        }

        @Override
        public void onWebSocketClose(final int statusCode, final String reason) {
            LOGGER.debug("The websocket was closed on status %d\nby reason of %s",
                    statusCode, reason);
        }

        @Override
        public void onWebSocketError(Throwable cause) {
            LOGGER.error("The websocket error");
        }

        @Override
        public void onWebSocketText(final String messageJson) {
            LOGGER.debug("Got some message");
        }
    }

}
