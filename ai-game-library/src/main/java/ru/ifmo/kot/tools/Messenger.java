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
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Created on 5/12/2016.
 */
public class Messenger {

    private static final String COMMAND_KEY = "command";
    private static final String RESPONSE_KEY = "requestStatus";
    private static final String ARGS_KEY = "args";

    public static class MessageEncoder
            implements Encoder.Text<Message> {

        @Override
        public void init(final EndpointConfig config) {
        }

        @SuppressWarnings("unchecked")
        @Override
        public String encode(final Message message)
                throws EncodeException {
            final JsonArrayBuilder argsArrayBuilder = Json.createArrayBuilder();
            for (final Object arg : message.getArgs()) {
                if (arg instanceof Integer) {
                    final Integer castedArg = (Integer) arg;
                    argsArrayBuilder.add(castedArg);
                } else if (arg instanceof String) {
                    final String castedArg = (String) arg;
                    argsArrayBuilder.add(castedArg);
                } else if (arg instanceof List) {
                    final List<String> castedArg = (List<String>) arg;
                    argsArrayBuilder.add(listAsJsonArray(castedArg));
                } else if (arg instanceof Map) {
                    final Map<String, String> castedArg = (Map<String, String>) arg;
                    argsArrayBuilder.add(mapAsJsonObj(castedArg));
                } else {
                    throw new IllegalArgumentException("The type is not supported by encoder");
                }
            }
            final JsonObjectBuilder objectBuilder = Json.createObjectBuilder()
                    .add(COMMAND_KEY, message.getCommand().name());
            final Optional<RequestStatus> optionalResponse = message.getRequestStatus();
            if (optionalResponse.isPresent()) {
                objectBuilder.add(RESPONSE_KEY, optionalResponse.get().name());
            }
            return
                    objectBuilder.add(ARGS_KEY, argsArrayBuilder.build()).build().toString();
        }

        private JsonArray listAsJsonArray (final List<String> stringList) {
            final JsonArrayBuilder argsArrayBuilder = Json.createArrayBuilder();
            stringList.forEach(argsArrayBuilder:: add);
            return argsArrayBuilder.build();
        }

        private JsonObject mapAsJsonObj (final Map<String, String> stringMap) {
            final JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
            stringMap.forEach(jsonObjectBuilder:: add);
            return jsonObjectBuilder.build();
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
            try (
                    final JsonReader reader = readerFactory.createReader(new StringReader(inputString))
            ) {
                final JsonObject jsonObject = reader.readObject();
                final Command command = Command.valueOf(jsonObject.getString(COMMAND_KEY));
                final Optional<String> optionalStatusString =
                        Optional.ofNullable(jsonObject.getString(RESPONSE_KEY, null));
                final Optional<RequestStatus> optionalRequestStatus;
                if (optionalStatusString.isPresent()) {
                    optionalRequestStatus =
                            Optional.of(RequestStatus.valueOf(optionalStatusString.get()));
                } else {
                    optionalRequestStatus = Optional.empty();
                }
                final List<JsonValue> argsList = jsonObject.getJsonArray(ARGS_KEY);
                final List<Object> objectArgsList =
                        argsList.stream().collect(Collectors.mapping(arg -> {
                            if (arg.getValueType().equals(JsonValue.ValueType.STRING)) {
                                return ((JsonString) arg).getString();
                            }
                            if (arg.getValueType().equals(JsonValue.ValueType.NUMBER)) {
                                return ((JsonNumber) arg).intValue();
                            }
                            if (arg.getValueType().equals(JsonValue.ValueType.ARRAY)) {
                                return ((JsonArray) arg).getValuesAs(JsonString.class);
                            }
                            if (arg.getValueType().equals(JsonValue.ValueType.OBJECT)) {
                                return ((JsonObject) arg).get
                            }
                            return null;
                        }, Collectors.toList()));
                final Object[] objectArgs = new Object[objectArgsList.size()];
                objectArgsList.toArray(objectArgs);
                if (optionalRequestStatus.isPresent()) {
                    final RequestStatus requestStatus = optionalRequestStatus.get();
                    message = new Message(command, requestStatus, objectArgs);
                } else {
                    message = new Message(command, objectArgs);
                }
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

        private final Command command;
        private final RequestStatus requestStatus;
        private final Object[] args;

        public Message(final Command command, final RequestStatus requestStatus, final Object... args) {
            this.command = command;
            this.requestStatus = requestStatus;
            this.args = args;
        }

        public Message(final Command command, final Object... args) {
            this.command = command;
            this.requestStatus = null;
            this.args = args;
        }

        public Command getCommand() {
            return command;
        }

        public Optional<RequestStatus> getRequestStatus() {
            return Optional.ofNullable(requestStatus);
        }

        public Object[] getArgs() {
            return args;
        }
    }

}
