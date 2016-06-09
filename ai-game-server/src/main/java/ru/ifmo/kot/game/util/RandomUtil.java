package ru.ifmo.kot.game.util;

import java.util.Random;

public class RandomUtil {

    private static final Random USUAL_RANDOM = new Random();

    public static double nextDouble(final double rangeMin, final double rangeMax) {
        return rangeMin + (rangeMax - rangeMin) * USUAL_RANDOM.nextDouble();
    }

}
