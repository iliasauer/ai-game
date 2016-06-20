package ru.ifmo.kot.game.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.ParameterizedMessage;
import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.aibase.Ai;
import ru.ifmo.kot.game.aibase.AiBase;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 29.05.16.
 */
public class RepeaterAi extends AiBase {

    private static final Random USUAL_RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getFormatterLogger(RepeaterAi.class);

    public RepeaterAi(final ServerApi api) {
        super(api);
    }

    private String name;

    @Override
    public String name() {
        final String name = Ai.generateName();
        this.name = name;
        return name;
    }

    @Override
    public String name(String missedParameter) {
        String name = Ai.generateName();
        while (name.equals(missedParameter)) {
            name = Ai.generateName();
        }
        this.name = name;
        return name;
    }

    @Override
    public String move() {
        final String currentVertex = api().currentVertex();
        final Map<String, String> competitorsPositions = api().whereAreCompetitors();
        competitorsPositions.remove(name);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("Internal error");
        }
        return competitorsPositions.values().iterator().next();
    }

}
