package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.game.util.RandomUtil;
import ru.ifmo.kot.game.visualiztion.ViewMessage;
import ru.ifmo.kot.game.visualiztion.VisualizationEndpoint;

import java.util.Objects;
;import static ru.ifmo.kot.game.elements.ElementsConstants.BENEFIT_MAX_FACTOR;
import static ru.ifmo.kot.game.elements.ElementsConstants.TURN_POINTS;

public class Player {

    private static final Logger LOGGER = LogManager.getFormatterLogger(Player.class);
    private static final int MIN_ACCELERATION_FACTOR = 2;
    private int numberOfLives = ElementsConstants.PLAYERS_LIVES_NUMBER;
    private final String name;
    private String currentPosition;
    private String expectedPosition = null;
    private int expectedPositionDistance = 0;
    private int tempAcceleration = 0;

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
            this.expectedPositionDistance -= (TURN_POINTS + tempAcceleration);
            tempAcceleration = 0;
            if (expectedPositionDistance <= 0) {
                currentPosition = expectedPosition;
                expectedPosition = null;
                LOGGER.info("The player %s arrived in %s", name, currentPosition);
                VisualizationEndpoint.sendMessage(new ViewMessage(name, currentPosition));
                return false;
            } else {
                LOGGER.info("The player %s goes to %s", name, expectedPosition);
                LOGGER.info("Left %d km", expectedPositionDistance);
                VisualizationEndpoint.sendMessage(new ViewMessage(name, currentPosition + "+" +
                        expectedPosition));
                return false;
            }
        } else {
            return true;
        }
    }

    public void enableTempAcceleration() {
        final double accelerationFactor =
                MIN_ACCELERATION_FACTOR + RandomUtil.nextDouble(MIN_ACCELERATION_FACTOR,
                        BENEFIT_MAX_FACTOR);
        this.tempAcceleration = (int) (TURN_POINTS * accelerationFactor);
    }

    public int getSpeed() {
        return TURN_POINTS;
    }

    public String getCurrentPosition() {
        return currentPosition;
    }

    public String getName() {
        return name;
    }

    public boolean removeLife() {
        numberOfLives--;
        if (numberOfLives > 0) {
            return true;
        } else {
            currentPosition = null;
            return false;
        }
    }
}
