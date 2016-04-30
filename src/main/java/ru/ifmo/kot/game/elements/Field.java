package ru.ifmo.kot.game.elements;

import java.io.File;

public class Field {

    private static final String VERTICES_FILE_PATH = "vertices.json";

    public void getVerticesFromFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(VERTICES_FILE_PATH).getFile());
    }

}
