package ru.ifmo.kot.game.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class BinaryRandom {

    private static final Logger LOGGER = LogManager.getLogger(BinaryRandom.class);

    private final Random javaRandom = new Random();
    private final double probability;

    public BinaryRandom(final double probability) {
        if (probability < 0 || probability > 1) {
            LOGGER.error("The wrong value of the probability");
            throw new IllegalArgumentException();
        }
        this.probability = probability;
    }

    public boolean nextBoolean() {
        return javaRandom.nextDouble() <= probability;
    }

}
