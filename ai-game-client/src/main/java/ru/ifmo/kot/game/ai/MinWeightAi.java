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
public class MinWeightAi extends AiBase {

    private static final Random USUAL_RANDOM = new Random();
    private static final Logger LOGGER = LogManager.getFormatterLogger(MinWeightAi.class);

    public MinWeightAi(final ServerApi api) {
        super(api);
    }

    private String prevVertex;

    @Override
    public String move() {
        final String currentVertex = api().currentVertex();
        final List<String> nextVertices = api().nextVertices(currentVertex);
        String minWeightVertex = nextVertices.get(0);
        if (minWeightVertex.equals(prevVertex) && nextVertices.size() > 1) {
            minWeightVertex = nextVertices.get(1);
        }
        int minWeight = api().weight(currentVertex, minWeightVertex);
        for (final String vertex : nextVertices) {
            if (! vertex.equals(prevVertex)) {
                int weight = api().weight(currentVertex, vertex);
                if (minWeight > weight) {
                    minWeight = weight;
                    minWeightVertex = vertex;
                }
            }
        }
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (final InterruptedException e) {
            LOGGER.error("Internal error");
        }
        prevVertex = currentVertex;
        return minWeightVertex;
    }

}
