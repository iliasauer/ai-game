package ru.ifmo.kot.game.visualiztion;

import ru.ifmo.kot.game.elements.Field;

import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;

/**
 Created on 12.06.16.
 */
@SuppressWarnings("WeakerAccess")
public class FieldEncoder implements Encoder.Text<Field> {
    
    @Override
    public String encode(final Field field)
    throws EncodeException {
        return field.asJson();
    }

    @Override
    public void init(final EndpointConfig config) {
    }

    @Override
    public void destroy() {
    }
}
