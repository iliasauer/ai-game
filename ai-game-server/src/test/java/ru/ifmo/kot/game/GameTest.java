package ru.ifmo.kot.game;

import org.junit.Test;

import javax.json.JsonObject;
import java.text.MessageFormat;

public class GameTest {
    @Test
    public void shouldLoadSettingsFromFile() throws Exception {
        final JsonObject settings = Game.getSettings().getJsonObject("server");
        settings.forEach((key, value) ->
                System.out.println(MessageFormat.format("{0}: {1}", key, value)));
    }

}