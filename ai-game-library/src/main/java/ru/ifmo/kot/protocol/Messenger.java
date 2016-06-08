package ru.ifmo.kot.protocol;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.json.*;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonParser;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 Created on 5/12/2016.
 */
public class Messenger {

	private static final Logger LOGGER = LogManager.getFormatterLogger(Messenger.class);
	private static final String COMMAND_KEY = "command";
	private static final String REQUEST_KEY = "requestStatus";
	private static final String RESPONSE_KEY = "responseStatus";
	private static final String ARGS_KEY = "args";

	public static class MessageEncoder
		implements Encoder.Text<Message> {

		@SuppressWarnings("ThrowableInstanceNeverThrown")
		private static final RuntimeException NOT_SUPPORTED_EXCEPTION =
			new IllegalArgumentException("The type is not supported by encoder");

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public String encode(final Message message)
		throws EncodeException {
			return encodeViaStream(message);
		}

		private String encodeViaStream(final Message message) {
			try(
				final Writer jsonStringWriter = new StringWriter();
			) {
				try(
					final JsonGenerator jsonGenerator = Json.createGenerator(jsonStringWriter);
				) {
					jsonGenerator.writeStartObject().write(
						COMMAND_KEY, message.getCommand().name());
					writeOptional(jsonGenerator, REQUEST_KEY, message.getRequestStatus());
					writeOptional(jsonGenerator, RESPONSE_KEY, message.getResponseStatus());
					writeArgs(jsonGenerator, message.getArgs());
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

		@SuppressWarnings("unchecked")
		private void writeArgs(final JsonGenerator generator, final Object[] args) {
			generator.writeStartArray(ARGS_KEY);
			Arrays.asList(args).stream().forEach(arg -> {
				if(arg instanceof Integer) {
					generator.write((Integer) arg);
				} else if(arg instanceof String) {
					generator.write((String) arg);
				} else if(arg instanceof Collection) {
					generator.writeStartArray();
					final Object item = ((Collection) arg).iterator().next();
					if(item instanceof String) {
						((Collection<String>) arg).forEach(generator:: write);
					} else {
						throw NOT_SUPPORTED_EXCEPTION;
					}
					generator.writeEnd();
				} else if(arg instanceof Map) {
					generator.writeStartObject();
					final Object item = ((Map) arg).values().iterator().next();
					if(item instanceof String) {
						((Map<String, String>) arg).forEach(generator:: write);
					} else {
						throw NOT_SUPPORTED_EXCEPTION;
					}
					generator.writeEnd();
				} else {
					throw NOT_SUPPORTED_EXCEPTION;
				}
			});
			generator.writeEnd();
		}

		@Override
		public void destroy() {
		}
	}

	public static class MessageDecoder
		implements Decoder.Text<Message> {

		@Override
		public void init(final EndpointConfig config) {
		}

		@Override
		public Message decode(final String inputString)
		throws DecodeException {
			return decodeViaStream(inputString);
		}


		private Message decodeViaStream(final String inputString) {
			Command command = Command.UNRECOGNIZABLE;
			Optional<RequestStatus> reqStatus = Optional.empty();
			Optional<ResponseStatus> respStatus = Optional.empty();
			Object[] args = {};
			try(
				final JsonParser parser = Json.createParser(new StringReader(inputString));
			) {
				while(parser.hasNext()) {
					switch(parser.next()) {
						case KEY_NAME:
							final String key = parser.getString();
							switch(key) {
								case COMMAND_KEY:
									parser.next();
									final String commandValue = parser.getString();
									if(Command.contains(commandValue)) {
										command = Command.valueOf(commandValue);
									}
									break;
								case REQUEST_KEY:
									parser.next();
									reqStatus =
										Optional.of(RequestStatus.valueOf(parser.getString()));
									break;
								case RESPONSE_KEY:
									parser.next();
									respStatus =
										Optional.of(ResponseStatus.valueOf(parser.getString()));
									break;
								case ARGS_KEY:
									parser.next(); // START_ARRAY
									args = parseArgs(parser);
									break;
							}
					}
				}
			}
			if(reqStatus.isPresent()) {
				return new Message(command, reqStatus.get(), args);
			}
			if(respStatus.isPresent()) {
				return new Message(command, respStatus.get(), args);
			}
			return new Message(command, args);
		}

		private Object[] parseArgs(final JsonParser jsonParser) {
			final List<Object> argsList = new ArrayList<>();
			while(jsonParser.hasNext()) {
				switch(jsonParser.next()) {
					case VALUE_STRING:
						argsList.add(jsonParser.getString());
						break;
					case VALUE_NUMBER:
						argsList.add(jsonParser.getInt());
						break;
					case START_ARRAY:
						final List<String> list = new ArrayList<>();
						listLoop:
						while(jsonParser.hasNext()) {
							switch(jsonParser.next()) {
								case VALUE_STRING:
									list.add(jsonParser.getString());
									break;
								case END_ARRAY:
									argsList.add(list);
									break listLoop;
							}
						}
						break;
					case START_OBJECT:
						final Map<String, String> map = new LinkedHashMap<>();
						mapLoop:
						while(jsonParser.hasNext()) {
							switch(jsonParser.next()) {
								case KEY_NAME:
									final String key = jsonParser.getString();
									jsonParser.next(); // to value
									final String value = jsonParser.getString();
									map.put(key, value);
									break;
								case END_OBJECT:
									argsList.add(map);
									break mapLoop;
							}
						}
						break;
					case END_ARRAY:
						return argsList.toArray();
				}
			}
			return argsList.toArray();
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

		private final Command command;
		private final RequestStatus requestStatus;
		private final ResponseStatus responseStatus;
		private final Object[] args;

		public Message(
			final Command command, final RequestStatus requestStatus, final Object... args
		) {
			this.command = command;
			this.requestStatus = requestStatus;
			this.responseStatus = null;
			this.args = args;
		}

		public Message(
			final Command command, final ResponseStatus responseStatus, final Object... args
		) {
			this.command = command;
			this.requestStatus = null;
			this.responseStatus = responseStatus;
			this.args = args;
		}

		public Message(final Command command, final Object... args) {
			this.command = command;
			this.requestStatus = null;
			this.responseStatus = null;
			this.args = args;
		}

		public Command getCommand() {
			return command;
		}

		public Optional<RequestStatus> getRequestStatus() {
			return Optional.ofNullable(requestStatus);
		}

		public Optional<ResponseStatus> getResponseStatus() {
			return Optional.ofNullable(responseStatus);
		}

		public Object[] getArgs() {
			return args;
		}
	}
}
