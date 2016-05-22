package ru.ifmo.kot.game;

import org.junit.Test;
import ru.ifmo.kot.tools.Messenger;

import javax.json.Json;
import javax.json.JsonObject;
import java.text.MessageFormat;
import java.util.Map;

import static org.junit.Assert.*;

public class GameTest {
    @Test
    public void shouldLoadSettingsFromFile() throws Exception {
        final JsonObject settings = Game.getSettings().getJsonObject("server");
        settings.forEach((key, value) ->
                System.out.println(MessageFormat.format("{0}: {1}", key, value)));
    }

}