package ru.ifmo.kot.tools;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.Collections;

/**
 * Created on 5/12/2016.
 */
public class Messenger {

    private static final String PLAYER_NAME_KEY = "playerName";
    private static final String CONTENT_KEY = "content";

    public static class MessageEncoder implements Encoder.Text<Message> {

        @Override
        public void init(final EndpointConfig config) {}

        @Override
        public String encode(final Message message) throws EncodeException {
            return Json.createObjectBuilder()
                    .add(PLAYER_NAME_KEY, message.getPlayerName())
                    .add(CONTENT_KEY, message.getContent())
                    .build()
                    .toString();
        }

        @Override
        public void destroy() {}

    }

    public static class MessageDecoder implements Decoder.Text<Message> {

        private final JsonReaderFactory readerFactory = Json.createReaderFactory(Collections.emptyMap());

        @Override
        public void init(final EndpointConfig config) {}

        @Override
        public Message decode(final String inputString) throws DecodeException {
            final Message message;
            try (final JsonReader reader = readerFactory.createReader(new StringReader(inputString))) {
                final JsonObject jsonObject = reader.readObject();
                final String playerName = jsonObject.getString(PLAYER_NAME_KEY);
                final String content = jsonObject.getString(CONTENT_KEY);
                message = new Message(playerName, content);
            }
            return message;
        }

        @Override
        public boolean willDecode(final String s) {
            return true;
        }

        @Override
        public void destroy() {}
    }

    public static class Message {

        private final String playerName;
        private final String content;

        public Message(final String playerName, final String content) {
            this.playerName = playerName;
            this.content = content;
        }

        public String getPlayerName() {
            return playerName;
        }

        public String getContent() {
            return content;
        }
    }

}
