package ru.ifmo.kot.game.server;

import org.eclipse.jetty.util.log.Logger;

public class SimplestLogger implements Logger {

    private boolean debuggingEnabling = false;

    @Override
    public String getName() {
        return "SimplestLogger";
    }

    private void consoleMessagePrint(final String msg) {
        System.out.println(msg);
    }

    private void consoleExceptionPrint(final Throwable thrown) {
        thrown.printStackTrace(System.out);
    }

    @Override
    public void warn(String msg, Object... args) {

    }

    @Override
    public void warn(Throwable thrown) {

    }

    @Override
    public void warn(String msg, Throwable thrown) {

    }

    @Override
    public void info(String msg, Object... args) {
//        consoleMessagePrint(msg);
    }

    @Override
    public void info(Throwable thrown) {
//        consoleExceptionPrint(thrown);
    }

    @Override
    public void info(String msg, Throwable thrown) {
//        info(msg);
//        info(thrown);
    }

    @Override
    public boolean isDebugEnabled() {
        return debuggingEnabling;
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        debuggingEnabling = enabled;
    }

    @Override
    public void debug(String msg, Object... args) {
        consoleMessagePrint(msg);
    }

    @Override
    public void debug(String msg, long value) {
        debug(msg);
    }

    @Override
    public void debug(Throwable thrown) {
        consoleExceptionPrint(thrown);
    }

    @Override
    public void debug(String msg, Throwable thrown) {
        debug(msg);
        debug(thrown);
    }

    @Override
    public Logger getLogger(String name) {
        if (name.equals(getName())) {
            return this;
        } else {
            return new Logger() {
                @Override
                public String getName() {
                    return null;
                }

                @Override
                public void warn(String msg, Object... args) {

                }

                @Override
                public void warn(Throwable thrown) {

                }

                @Override
                public void warn(String msg, Throwable thrown) {

                }

                @Override
                public void info(String msg, Object... args) {

                }

                @Override
                public void info(Throwable thrown) {

                }

                @Override
                public void info(String msg, Throwable thrown) {

                }

                @Override
                public boolean isDebugEnabled() {
                    return false;
                }

                @Override
                public void setDebugEnabled(boolean enabled) {

                }

                @Override
                public void debug(String msg, Object... args) {

                }

                @Override
                public void debug(String msg, long value) {

                }

                @Override
                public void debug(Throwable thrown) {

                }

                @Override
                public void debug(String msg, Throwable thrown) {

                }

                @Override
                public Logger getLogger(String name) {
                    return null;
                }

                @Override
                public void ignore(Throwable ignored) {

                }
            };
        }
    }

    @Override
    public void ignore(Throwable ignored) {

    }
}
