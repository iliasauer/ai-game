package ru.ifmo.kot.game.visualiztion;

import ru.ifmo.kot.game.model.EdgeContent;

public class EventMessage {

    private final String name;
    private final String element;
    private final EdgeContent content;

    public EventMessage(String name, String element) {
        this.name = name;
        this.element = element;
        this.content = null;
    }

    public EventMessage(String name, String element, EdgeContent content) {
        this.name = name;
        this.element = element;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getElement() {
        return element;
    }

    public EdgeContent getContent() {
        return content;
    }
}
