package ru.ifmo.kot.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

/**
 * Created on 05.06.16.
 */
public class WaitingForResponseTask implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(WaitingForResponseTask.class);
    private final BooleanSupplier valueChecker;

    public WaitingForResponseTask(final BooleanSupplier valueChecker) {
        this.valueChecker = valueChecker;
    }

    public Void call() {
        while (!valueChecker.getAsBoolean()) {
            try {
                TimeUnit.NANOSECONDS.sleep(10L);
            } catch (final InterruptedException e) {
                LOGGER.error("Internal error");
            }
        }
        return null;
    }
}
