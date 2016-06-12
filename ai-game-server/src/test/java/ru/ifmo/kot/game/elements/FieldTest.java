package ru.ifmo.kot.game.elements;

import org.junit.BeforeClass;
import org.junit.Test;
import ru.ifmo.kot.game.model.Edge;
import ru.ifmo.kot.game.model.Graph;
import ru.ifmo.kot.game.model.SymbolGraph;

import static org.junit.Assert.*;

public class FieldTest {

    private static SymbolGraph gameModel;

    @BeforeClass
    public static void initField() {
        final Field field = new Field();
        gameModel = field.getGameModel();
    }

}