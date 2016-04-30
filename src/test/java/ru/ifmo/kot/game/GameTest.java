package ru.ifmo.kot.game;

import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class GameTest {
    @Test
    public void shouldLoadSettingsFromFile() throws Exception {
        final Map<String, Object> settings = Game.getSettings();
        for (Map.Entry settingPair: settings.entrySet()) {
            System.out.println(settingPair.getKey() + ": " + settingPair.getValue());
        }
    }

}