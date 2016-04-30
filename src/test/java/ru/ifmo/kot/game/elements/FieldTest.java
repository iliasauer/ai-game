package ru.ifmo.kot.game.elements;

import org.junit.Test;

import static org.junit.Assert.*;

public class FieldTest {

    @Test
    public void shouldCreateSimplestField() {
        Field field = new Field();
        field.getGameModel().printAdjacencyMatrix();
        field.printVertices();
    }

}