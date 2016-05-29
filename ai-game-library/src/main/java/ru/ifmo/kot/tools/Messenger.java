package ru.ifmo.kot.tools;

import javax.json.*;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created on 5/12/2016.
 */
public class Messenger {

	private static final String COMMAND_KEY = "command";
	private static final String ARGS_KEY = "args";

	public static class MessageEncoder
		implements Encoder.Text<Message> {

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public String encode(final Message message)
		throws EncodeException {
			final JsonArrayBuilder argsArrayBuilder = Json.createArrayBuilder();
			for (final Object arg: message.getArgs()) {
                if (arg instanceof Integer) {
                    final Integer castedArg = (Integer) arg;
                    argsArrayBuilder.add(castedArg);
                } else if (arg instanceof String) {
                    final String castedArg = (String) arg;
                    argsArrayBuilder.add(castedArg);
                }
			}
			return Json.createObjectBuilder()
				.add(COMMAND_KEY, message.getCommand())
				.add(ARGS_KEY, argsArrayBuilder.build())
				.build().toString();
		}

		@Override
		public void destroy() {
		}
	}

	public static class MessageDecoder
		implements Decoder.Text<Message> {

		private final JsonReaderFactory readerFactory =
			Json.createReaderFactory(Collections.emptyMap());

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public Message decode(final String inputString)
		throws DecodeException {
			final Message message;
			try(
				final JsonReader reader = readerFactory.createReader(new StringReader(inputString))
			) {
				final JsonObject jsonObject = reader.readObject();
				final String command = jsonObject.getString(COMMAND_KEY);
				final List<JsonValue> argsList = jsonObject.getJsonArray(ARGS_KEY);
                final List<Object> objectArgsList =
                argsList.stream().collect(Collectors.mapping(arg -> {
                    if (arg.getValueType().equals(JsonValue.ValueType.STRING)) {
                        return ((JsonString) arg).getString();
                    }
                    if (arg.getValueType().equals(JsonValue.ValueType.NUMBER)) {
                        return ((JsonNumber) arg).intValue();
                    }
                    return null;
                }, Collectors.toList()));
                final Object[] objectArgs = new Object[objectArgsList.size()];
                objectArgsList.toArray(objectArgs);
				message = new Message(command, objectArgs);
			}
			return message;
		}

		@Override
		public boolean willDecode(final String s) {
			return true;
		}

		@Override
		public void destroy() {
		}
	}

	public static class Message {

		private final String command;
		private final Object[] args;

        public Message(final String command, final Object ... args) {
            this.command = command;
            this.args = args;
        }

		public String getCommand() {
			return command;
		}

		public Object[] getArgs() {
			return args;
		}
	}

}
