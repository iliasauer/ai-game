package ru.ifmo.kot.tools;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonReaderFactory;
import javax.json.JsonValue;
import javax.websocket.DecodeException;
import javax.websocket.Decoder;
import javax.websocket.EncodeException;
import javax.websocket.Encoder;
import javax.websocket.EndpointConfig;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 Created on 5/12/2016.
 */
public class Messenger {

	private static final String PLAYER_NAME_KEY = "playerName";
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
			for (final String arg: message.getArgs()) {
				argsArrayBuilder.add(arg);
			}
			return Json.createObjectBuilder()
				.add(PLAYER_NAME_KEY, message.getPlayerName())
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
				final String playerName = jsonObject.getString(PLAYER_NAME_KEY);
				final String command = jsonObject.getString(COMMAND_KEY);
				final List<String> argsList = jsonObject.getJsonArray(ARGS_KEY).stream().collect(
					Collectors.mapping(JsonValue:: toString, Collectors.toList()));
				String[] args = new String[argsList.size()];
				argsList.toArray(args);
				message = new Message(playerName, command, args);
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

		private final String playerName;
		private final String command;
		private final String[] args;

		public Message(final String playerName, final String command, String ... args) {
			this.playerName = playerName;
			this.command = command;
			this.args = args;
		}

		public String getPlayerName() {
			return playerName;
		}

		public String getCommand() {
			return command;
		}

		public String[] getArgs() {
			return args;
		}
	}

	public static String handleMessageString(final String string) {
		return string.substring(1, string.length() - 1);
	}
}
