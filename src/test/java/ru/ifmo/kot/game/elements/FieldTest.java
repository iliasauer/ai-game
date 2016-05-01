package ru.ifmo.kot.game.elements;

import org.junit.Test;
import ru.ifmo.kot.game.model.Graph;

import static org.junit.Assert.*;

public class FieldTest {
    @Test
    public void shouldCheckGameModel() throws Exception {
        final Field field = new Field();
        final Graph gameModel = field.getGameModel();
        gameModel.printVertices();
        gameModel.printAdjacencyMatrix();
    }

}