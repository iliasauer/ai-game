package ru.ifmo.kot.game.server;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Logger;
import org.eclipse.jetty.webapp.WebAppContext;

import static ru.ifmo.kot.game.server.ServerConstants.*;

public class MainServer {

    static { Log.setLog(new SimplestLogger()); }
    private static final Logger LOGGER = Log.getLogger("SimplestLogger");
    static { LOGGER.setDebugEnabled(false); }

    private static Object getSetting(final String key) {
        return SETTINGS.get(key);
    }

    public static void main(final String[] args) {
        final Server server = new Server((Integer) getSetting(PORT_KEY));
        final WebAppContext context = new WebAppContext();
        context.setContextPath((String) getSetting(CONTEXT_PATH_KEY));
        context.setResourceBase(WEBAPP_PATH);
        context.setDescriptor(WEBXML_PATH);
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
