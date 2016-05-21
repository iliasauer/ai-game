package ru.ifmo.kot.tools;

import org.junit.Test;

import javax.json.*;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Created on 5/21/2016.
 */
public class MessengerTest {

    private static final String NAME_KEY = "name";
    private static final String AGE_KEY = "age";
    private static final String IS_MARRIED_KEY = "isMarried";
    private static final String ADDRESS_KEY = "address";
    private static final String STREET_KEY = "street";
    private static final String ZIP_CODE_KEY = "zipCode";
    private static final String PHONE_NUMBERS_KEY = "phoneNumbers";

    private static final String PERSON_JSON_DATA =
            "  {" +
                    "   \"name\": \"Jack\", " +
                    "   \"age\" : 13, " +
                    "   \"isMarried\" : false, " +
                    "   \"address\": { " +
                    "     \"street\": \"#1234, Main Street\", " +
                    "     \"zipCode\": 123456" +
                    "   }, " +
                    "   \"phoneNumbers\": [\"011-111-1111\", \"11-111-1111\"] " +
                    " }";

    private static final String PERSON_JSON_DATA_ARRAY = "[" + PERSON_JSON_DATA + "]";

    private static class Person {
        private String name;
        private int age;
        private boolean isMarried;

        Person(String name, int age, boolean isMarried) {
            this.name = name;
            this.age = age;
            this.isMarried = isMarried;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isMarried() {
            return isMarried;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Name: {0}\nAge: {1}\nIs married: {2}\n",
                    name, age, isMarried);
        }
    }

    private static final List<Person> NAMES = new ArrayList<>();

    static {
        NAMES.add(new Person("Ilya", 21, false));
        NAMES.add(new Person("Oleg", 51, true));
        NAMES.add(new Person("Marina", 53, true));
        NAMES.add(new Person("__Olga", 49, true));
        NAMES.add(new Person("__Konstantin", 50, true));
    }

    private void processSuitableNames(final List<Person> persons,
                                      final Predicate<Person> tester,
                                      final Function<Person, String> mapper,
                                      final Consumer<String> actor) {
        for (final Person person: persons) {
            if (tester.test(person)) {
                final String data = mapper.apply(person);
                actor.accept(data);
            }
        }
    }

    @Test
    public void shouldReadJsonObject() throws Exception {
        final JsonObject personObject;
        try(final JsonReader reader = Json.createReader(new StringReader(PERSON_JSON_DATA))) {
             personObject = reader.readObject();
        }
        System.out.println(MessageFormat.format("Name: {0}", personObject.getString(NAME_KEY)));
        System.out.println(MessageFormat.format("Age: {0}", personObject.getInt(AGE_KEY)));
        System.out.println(MessageFormat.format("Married: {0}", personObject.getBoolean(IS_MARRIED_KEY)));
        final JsonObject addressObject = personObject.getJsonObject(ADDRESS_KEY);
        System.out.println("Address: ");
        System.out.println(MessageFormat.format("\tStreet: {0}", addressObject.getString(STREET_KEY)));
        System.out.println(MessageFormat.format("\tZip code: {0}", addressObject.getInt(ZIP_CODE_KEY)));
        final JsonArray phoneNumbersArray = personObject.getJsonArray(PHONE_NUMBERS_KEY);
        for (final JsonValue jsonValue: phoneNumbersArray) {
            System.out.println(jsonValue.toString());
        }
    }

    @Test
    public void shouldReadJsonArray() {
        final JsonArray personArray;
        try(final JsonReader reader = Json.createReader(new StringReader(PERSON_JSON_DATA_ARRAY))) {
            personArray = reader.readArray();
        }
        for (final JsonValue personObj: personArray) {
            System.out.println(MessageFormat.format("{0} - {1}", personObj.getValueType(),
                    ((JsonObject) personObj).getString(NAME_KEY)));
        }
    }

    @Test
    public void shouldWriteJsonObject() {
        final JsonObject personObject = Json.createObjectBuilder()
                .add(NAME_KEY, "John")
                .add(AGE_KEY, 13)
                .add(IS_MARRIED_KEY, false)
                .add(ADDRESS_KEY, Json.createObjectBuilder()
                        .add(STREET_KEY, "#1234, Main Street")
                        .add(ZIP_CODE_KEY, 123456)
                        .build()
                )
                .add(PHONE_NUMBERS_KEY, Json.createArrayBuilder()
                        .add("011-111-1111")
                        .add("11-111-1111")
                        .build()
                )
                .build();

        System.out.println("Object: " + personObject);
    }

    @Test
    public void shouldExecuteSimpleLambdaExpression() {
        processSuitableNames(NAMES,
                person -> !person.getName().startsWith("_"),
                person -> person.getName(),
                name -> System.out.println(name) );
    }

}