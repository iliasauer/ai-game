package ru.ifmo.kot.game.elements;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.kot.game.model.*;

import static org.junit.Assert.*;

public class FieldTest {

    private static SymbolGraph gameModel;

    @BeforeClass
    public static void initField() {
        final Field field = new Field();
        gameModel = field.getGameModel();
    }

    @Test
    public void shouldChangeWeight() throws Exception {
        Graph graph = gameModel.graph();
        int srcVrtxIndx = 0;
        int dstVrtxIndx = 0;
        int counter = 0;
        for (Edge edge: graph.edges()) {
            srcVrtxIndx = edge.anyVertexIndex();
            dstVrtxIndx = edge.otherVertexIndex(srcVrtxIndx);
            counter++;
            if (counter >= 5) {
                break;
            }
        }
        String srcVrtxName = gameModel.name(srcVrtxIndx);
        String dstVrtxName = gameModel.name(dstVrtxIndx);
        final int newWeight = 333;
        gameModel.putEdge(srcVrtxName, dstVrtxName, newWeight);
        assertEquals(newWeight, gameModel.getWeight(srcVrtxName, dstVrtxName));
    }

    @Test
    public void shouldHaveSpanningTree() throws Exception {
        Graph graph = gameModel.graph();
        assertTrue(graph.hasSpanningTree());
    }

}