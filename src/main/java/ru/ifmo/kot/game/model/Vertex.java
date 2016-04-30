package ru.ifmo.kot.game.model;

public class Vertex {

    private final int index;
    private final String name;

    public Vertex(int index, String name) {
        this.index = index;
        this.name = name;
    }

    @Override
    public String toString() {
        return index + ": " + name;
    }
}
