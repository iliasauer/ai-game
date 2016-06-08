package ru.ifmo.kot.protocol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created on 5/21/2016.
 */
public class MessengerTest {




//    @Test
//    public void shouldReadJsonObject() throws Exception {
//        final JsonObject personObject;
//        try(final JsonReader reader = Json.createReader(new StringReader(PERSON_JSON_DATA))) {
//             personObject = reader.readObject();
//        }
//        System.out.println(MessageFormat.format("Name: {0}", personObject.getString(NAME_KEY)));
//        System.out.println(MessageFormat.format("Age: {0}", personObject.getInt(AGE_KEY)));
//        System.out.println(MessageFormat.format("Married: {0}", personObject.getBoolean(IS_MARRIED_KEY)));
//        final JsonObject addressObject = personObject.getJsonObject(ADDRESS_KEY);
//        System.out.println("Address: ");
//        System.out.println(MessageFormat.format("\tStreet: {0}", addressObject.getString(STREET_KEY)));
//        System.out.println(MessageFormat.format("\tZip code: {0}", addressObject.getInt(ZIP_CODE_KEY)));
//        final JsonArray phoneNumbersArray = personObject.getJsonArray(PHONE_NUMBERS_KEY);
//        for (final JsonValue jsonValue: phoneNumbersArray) {
//            System.out.println(jsonValue.toString());
//        }
//    }
//
//    @Test
//    public void shouldReadJsonArray() {
//        final JsonArray personArray;
//        try(final JsonReader reader = Json.createReader(new StringReader(PERSON_JSON_DATA_ARRAY))) {
//            personArray = reader.readArray();
//        }
//        for (final JsonValue personObj: personArray) {
//            System.out.println(MessageFormat.format("{0} - {1}", personObj.getValueType(),
//                    ((JsonObject) personObj).getString(NAME_KEY)));
//        }
//    }
//
//    @Test
//    public void shouldWriteJsonObject() {
//        final JsonObject personObject = Json.createObjectBuilder()
//                .add(NAME_KEY, "John")
//                .add(AGE_KEY, 13)
//                .add(IS_MARRIED_KEY, false)
//                .add(ADDRESS_KEY, Json.createObjectBuilder()
//                        .add(STREET_KEY, "#1234, Main Street")
//                        .add(ZIP_CODE_KEY, 123456)
//                        .build()
//                )
//                .add(PHONE_NUMBERS_KEY, Json.createArrayBuilder()
//                        .add("011-111-1111")
//                        .add("11-111-1111")
//                        .build()
//                )
//                .build();
//
//        System.out.println("Object: " + personObject);
//    }

}