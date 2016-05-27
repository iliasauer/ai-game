package ru.ifmo.kot.game.elements;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;;

public class Player {

    private static final Logger LOGGER = LogManager.getFormatterLogger(Player.class);
    private static List<Player> players = new ArrayList<>();
    private static Set<String> names = new HashSet<>();
    private static int NUMBER_OF_LIVES = 3;

    public static boolean addPlayer(final String name) {
        if (isNameOccupied(name)) {
            return false;
        } else {
            players.add(new Player(name));
            names.add(name);
            return true;
        }
    }

    public static void removePlayer(final String name) {
        players.stream()
            .filter(player -> player.name.equals(name)).findFirst()
            .ifPresent(player -> {
                players.remove(player);
                names.remove(player.name);
            });
    }

    public static void removePlayer(final int index) {
        afterCheckIndex(index, (Consumer<Integer>) players::remove);
    }

    public static Player getPlayer(final String name) {
        return players.stream().filter(player -> player.name.equals(name)).findFirst().orElse(null);
    }

    public static Player getPlayer(final int index) {
        return afterCheckIndex(index, players::get);
    }

    private static void afterCheckIndex(final int index, final Consumer<Integer> consumer) {
        if (index >=0 && index < players.size()) {
            consumer.accept(index);
        }
    }

    private static Player afterCheckIndex(final int index, final Function<Integer, Player> function) {
        if (index >=0 && index < players.size()) {
            return function.apply(index);
        }
        return null;
    }


    private static boolean isNameOccupied(final String name) {
        return names.contains(name);
    }

    private final String name;
    private String currentPosition;

    private Player(final String name) {
        this.name = name;
    }

    public void setCurrentPosition(String currentPosition) {
        this.currentPosition = currentPosition;
    }


    public String getCurrentPosition() {
        return currentPosition;
    }
}
