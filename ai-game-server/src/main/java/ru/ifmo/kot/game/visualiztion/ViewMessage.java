package ru.ifmo.kot.game.visualiztion;

import ru.ifmo.kot.game.model.EdgeContent;

public class ViewMessage {

    private final Type type;
    private final String name;
    private final String element;
    private final Event event;

    public ViewMessage(final String name, final String element) {
        this.type = Type.MOVE;
        this.name = name;
        this.element = element;
        this.event = null;
    }

    public ViewMessage(final String name, final Event content) {
        this.type = Type.EVENT;
        this.name = name;
        this.element = null;
        this.event = content;
    }

    public String getName() {
        return name;
    }

    public String getElement() {
        return element;
    }

    public Event getEvent() {
        return event;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        MOVE, EVENT
    }
}
