package ru.ifmo.kot.protocol;

import java.util.Arrays;

public enum Command {
    NAME,
    START_DATA,
    MOVE,
    CURRENT_VERTEX,
    NEXT_VERTICES,
    WEIGHT,
    COMPETITORS_POSITIONS,
    LOSE,
    WIN,
    UNRECOGNIZABLE;

    public static boolean contains(final String name) {
       return Arrays.asList(Command.values()).stream()
            .anyMatch(command -> command.name().equals(name));
    }
}
