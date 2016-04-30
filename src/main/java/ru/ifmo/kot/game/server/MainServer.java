package ru.ifmo.kot.game.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

public class MainServer {

    static {
        Log.setLog(new SimplestLogger());
    }
    private static final Logger LOGGER = Log.getLogger("SimplestLogger");
    static {
        LOGGER.setDebugEnabled(false);
    }


    public static void main(final String[] args) {
        final Server server = new Server(ServerConstants.PORT);
        final WebAppContext context = new WebAppContext();
        context.setContextPath(ServerConstants.WEBAPP_CONTEXT_PATH);
        context.setResourceBase(ServerConstants.WEBAPP_PATH);
        context.setDescriptor(ServerConstants.WEBXML_PATH);
        context.setParentLoaderPriority(true);

        server.setHandler(context);
        try {
            server.start();
            LOGGER.debug("The game server started.");
            server.join();
        } catch (Exception e) {
            LOGGER.debug("The game server cannot started.", e);
        }
    }
}
