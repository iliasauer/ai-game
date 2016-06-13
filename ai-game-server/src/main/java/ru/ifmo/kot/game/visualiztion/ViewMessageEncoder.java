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
public class ViewMessageEncoder implements Encoder.Text<ViewMessage> {

    private static final Logger LOGGER = LogManager.getFormatterLogger(ViewMessageEncoder.class);
    private static final String TYPE_KEY = "type";
    private static final String PLAYER_KEY = "player";
    private static final String ELEMENT_KEY = "element";
    private static final String EVENT_KEY = "event";

    @Override
    public String encode(final ViewMessage viewMessage) throws EncodeException {
        try(
                final Writer jsonStringWriter = new StringWriter();
        ) {
            try(
                    final JsonGenerator jsonGenerator = Json.createGenerator(jsonStringWriter);
            ) {
                final ViewMessage.Type type = viewMessage.getType();
                jsonGenerator.writeStartObject()
                        .write(TYPE_KEY, type.name())
                        .write(PLAYER_KEY, viewMessage.getName());
                switch (type) {
                    case MOVE:
                        jsonGenerator.write(ELEMENT_KEY, viewMessage.getElement());
                        break;
                    case EVENT:
                        jsonGenerator.write(EVENT_KEY, viewMessage.getEvent().name());
                        break;
                }
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
