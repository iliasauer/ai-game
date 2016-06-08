package ru.ifmo.kot.protocol;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created on 5/21/2016.
 */
public class MessengerTest {

    private static int number;
    private static String string;
    private static List<String> list;
    private static Map<String, String> map;

    @BeforeClass
    public static void setUpArgsData() {
        number = 365;
        string = "Perm";
        final List<String> tempList = new ArrayList<>();
        tempList.add("Kaliningrad");
        tempList.add("Kazan");
        list = Collections.unmodifiableList(tempList);
        final Map<String, String> tempMap = new LinkedHashMap<>();
        tempMap.put("player2", "Moscow");
        tempMap.put("player3", "St.Petersburg");
        map = Collections.unmodifiableMap(tempMap);
    }

    private static String messageString;
    private Messenger.Message message;

    @Before
    public void setUpMessage() {
        message = new Messenger.Message(
            Command.UNRECOGNIZABLE,
            ResponseStatus.OK,
            number, string, list, map);
    }

    @Before
    public void setUpMessageString() {
        messageString = "{\"command\":\"UNRECOGNIZABLE\"," +
            "\"responseStatus\":\"OK\"," +
            "\"args\":[" +
            "365," +
            "\"Perm\"," +
            "[\"Kaliningrad\",\"Kazan\"]," +
            "{\"player2\":\"Moscow\",\"player3\":\"St.Petersburg\"}]}";
    }

    private void printMessage() {
        System.out.println(message);
    }

    private void printMessageString() {
        System.out.println(messageString);
    }

    @Test
    public void shouldEncodeMessage() throws Exception {
        final Messenger.MessageEncoder messageEncoder = new Messenger.MessageEncoder();
        messageString = messageEncoder.encode(message);
        printMessageString();
    }

    @Ignore
    @Test
    public void shouldThrowEncodeMessageException() throws Exception {
        final Messenger.Message wrongMessage = new Messenger.Message(
            Command.UNRECOGNIZABLE,
            new HashSet<>());
        final Messenger.MessageEncoder messageEncoder = new Messenger.MessageEncoder();
        messageEncoder.encode(wrongMessage);
    }

    @Test
    public void shouldDecodeMessage() throws Exception {
        final Messenger.MessageDecoder messageDecoder = new Messenger.MessageDecoder();
        message = messageDecoder.decode(messageString);
        printMessage();
    }

}