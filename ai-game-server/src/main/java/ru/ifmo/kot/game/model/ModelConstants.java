package ru.ifmo.kot.game.model;

import ru.ifmo.kot.game.Engine;

import javax.json.JsonObject;

interface ModelConstants {

    String SETTINGS_KEY = "model";
    String COEFFICIENT_OF_EDGE_CONTENT_NUMBER_KEY = "contentCoefficient";
    String THRESHOLD_OF_OBSTACLE_KEY = "obstacleThreshold";
    String THRESHOLD_OF_BENEFIT_KEY = "benefitThreshold";
    JsonObject SETTINGS = Engine.getSettings().getJsonObject(SETTINGS_KEY);
    double COEFFICIENT_OF_EDGE_CONTENT_NUMBER =
            SETTINGS.getJsonNumber(COEFFICIENT_OF_EDGE_CONTENT_NUMBER_KEY).doubleValue();
    double THRESHOLD_OF_OBSTACLE =
            SETTINGS.getJsonNumber(THRESHOLD_OF_OBSTACLE_KEY).doubleValue();
    double THRESHOLD_OF_BENEFIT =
            SETTINGS.getJsonNumber(THRESHOLD_OF_BENEFIT_KEY).doubleValue();
}
