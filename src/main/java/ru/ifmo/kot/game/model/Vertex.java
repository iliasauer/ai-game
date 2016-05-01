package ru.ifmo.kot.game.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Vertex {

    private final String name;

    public Vertex(final String name) {
        this.name = name;
//        edges = new ArrayList<>();
    }

    public void addEdge(final Edge edge) {
//        edges.add(edge);
    }

    public String name() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }
}
