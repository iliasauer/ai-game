package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;;

public class Player {

    private static final Logger LOGGER = LogManager.getFormatterLogger(Player.class);
    private static final int TURN_SPEED = 100;

    private int numberOfLives = 3;
    private final String name;
    private String currentPosition;
    private String expectedPosition = null;
    private int expectedPositionDistance = 0;

    public Player(final String name, final String startPosition) {
        this.name = name;
        this.currentPosition = startPosition;
    }

    public void setExpectedPosition(final String expectedPosition, final int initDistance) {
        this.expectedPosition = expectedPosition;
        this.expectedPositionDistance = initDistance;
        LOGGER.info("The player %s wants to go to %s", name, expectedPosition);
        LOGGER.info("Left %d km", expectedPositionDistance);
        getCloseToExpectedPosition();
    }

    public boolean getCloseToExpectedPosition() {
        if (Objects.nonNull(expectedPosition)) {
            this.expectedPositionDistance -= TURN_SPEED;
            if (expectedPositionDistance <= 0) {
                currentPosition = expectedPosition;
                expectedPosition = null;
                LOGGER.info("The player %s arrived in %s", name, currentPosition);
                return false;
            } else {
                LOGGER.info("The player %s goes to %s", name, expectedPosition);
                LOGGER.info("Left %d km", expectedPositionDistance);
                return false;
            }
        } else {
            return true;
        }
    }


    public String getCurrentPosition() {
        return currentPosition;
    }

    public String getName() {
        return name;
    }

    public boolean removeLife() {
        numberOfLives--;
        return numberOfLives > 0;
    }
}
