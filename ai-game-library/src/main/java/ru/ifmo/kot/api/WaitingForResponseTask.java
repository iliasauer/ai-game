package ru.ifmo.kot.api;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Created on 05.06.16.
 */
public class WaitingForResponseTask<T> implements Callable<Void> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(WaitingForResponseTask.class);
    private final String mapKey;
    private final Predicate<String> valueChecker;

    public WaitingForResponseTask(final String mapKey, final Predicate<String> valueChecker) {
        this.mapKey = mapKey;
        this.valueChecker = valueChecker;
    }

    public Void call() {
        while (valueChecker.negate().test(mapKey)) {
            try {

                TimeUnit.MILLISECONDS.sleep(1L);
            } catch (final InterruptedException e) {
                LOGGER.error("Internal server error");
            }
        }
        return null;
    }
}
