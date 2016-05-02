package ru.ifmo.kot.game.elements;

import org.junit.Test;
import ru.ifmo.kot.game.model.SymbolGraph;
import ru.ifmo.kot.game.model.UndirectedWeightedGraph;

public class FieldTest {
    @Test
    public void shouldCheckGameModel() throws Exception {
        final Field field = new Field();
        final SymbolGraph gameModel = field.getGameModel();
        gameModel.printVertices();
        gameModel.printEdges();
    }

}