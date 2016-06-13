package ru.ifmo.kot.game.visualiztion;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Optional;

@SuppressWarnings("WeakerAccess")
public class EventEncoder implements Encoder.Text<EventMessage> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(EventEncoder.class);
    private static final String PLAYER_KEY = "player";
    private static final String ELEMENT_KEY = "element";
    private static final String CONTENT_KEY = "content";

    @Override
    public String encode(final EventMessage evtMsg) throws EncodeException {
        try(
                final Writer jsonStringWriter = new StringWriter();
        ) {
            try(
                    final JsonGenerator jsonGenerator = Json.createGenerator(jsonStringWriter);
            ) {
                jsonGenerator.writeStartObject()
                        .write(PLAYER_KEY, evtMsg.getName())
                        .write(ELEMENT_KEY, evtMsg.getElement());
                        writeOptional(jsonGenerator, CONTENT_KEY,
                                Optional.ofNullable(evtMsg.getContent()));
                jsonGenerator.writeEnd();
                jsonGenerator.flush();
                return jsonStringWriter.toString();
            }
        } catch(final IOException e) {
            LOGGER.error("Failed to encode the message");
        }
        return null;
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private <T extends Enum> void writeOptional(
            final JsonGenerator jsonGenerator, final String key, final Optional<T> optValue
    ) {
        if(optValue.isPresent()) {
            jsonGenerator.write(key, optValue.get().name());
        }
    }

    @Override
    public void init(final EndpointConfig config) {

    }

    @Override
    public void destroy() {

    }
}
