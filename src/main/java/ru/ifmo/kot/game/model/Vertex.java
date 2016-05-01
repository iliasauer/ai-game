package ru.ifmo.kot.game.model;

public class Vertex {

    private final String name;

    public Vertex(final String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }
}
