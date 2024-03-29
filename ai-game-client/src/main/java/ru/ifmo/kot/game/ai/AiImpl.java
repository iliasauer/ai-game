package ru.ifmo.kot.game.ai;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ru.ifmo.kot.api.ServerApi;
import ru.ifmo.kot.game.aibase.AiBase;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created on 29.05.16.
 */
public class AiImpl extends AiBase {

    private static final Random USUAL_RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getFormatterLogger(AiImpl.class);

    public AiImpl(final ServerApi api) {
        super(api);
    }

    private String currentVertex;

    @Override
    public String move() {
        final String currentVertex = api().currentVertex();
        final List<String> nextVertices = api().nextVertices(currentVertex);
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            LOGGER.error("Internal error");
        }
        return nextVertices.get(USUAL_RANDOM.nextInt(nextVertices.size()));
    }

}
