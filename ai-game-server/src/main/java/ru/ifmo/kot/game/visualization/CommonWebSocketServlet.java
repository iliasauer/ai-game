package ru.ifmo.kot.game.visualization;

import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

public class CommonWebSocketServlet extends WebSocketServlet {
    @Override
    public void configure(WebSocketServletFactory factory) {
        factory.register(CommonWebSocket.class);
        factory.getPolicy().setIdleTimeout(100000000); //TODO check it
    }
}
