package ru.ifmo.kot.tools;

import org.junit.Test;

import javax.json.*;

import java.io.StringReader;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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

    private static final class Person {
        private String name;
        private int age;
        private boolean isMarried;

        Person(final String name, final int age, final boolean isMarried) {
            this.name = name;
            this.age = age;
            this.isMarried = isMarried;
        }

        String getName() {
            return name;
        }

        int getAge() {
            return age;
        }

        boolean isMarried() {
            return isMarried;
        }

        @Override
        public String toString() {
            return MessageFormat.format("Name: {0}\nAge: {1}\nIs married: {2}\n",
                    name, age, isMarried);
        }
    }

    private static final List<Person> PERSONS = new ArrayList<>();

    static {
        PERSONS.add(new Person("Ilya", 21, false));
        PERSONS.add(new Person("Oleg", 51, true));
        PERSONS.add(new Person("Marina", 53, true));
        PERSONS.add(new Person("__Olga", 49, true));
        PERSONS.add(new Person("__Konstantin", 50, true));
    }

    private <X, Y> void processData(final Iterable<X> data,
                                    final Predicate<X> tester,
                                    final Function<X, Y> mapper,
                                    final Consumer<Y> actor) {
        for (final X aData: data) {
            if (tester.test(aData)) {
                final Y item = mapper.apply(aData);
                actor.accept(item);
            }
        }
    }

    private <X, Y> void processDataInStream(final List<X> data,
                                            final Predicate<X> tester,
                                            final Function<X, Y> mapper,
                                            final Consumer<Y> actor) {
        data.stream().filter(tester).map(mapper).forEach(actor);
    }

    private <X, Y> List<Y> collectSuitableItemsToList(final List<X> data,
                                          final Predicate<X> tester,
                                          final Function<X, Y> mapper) {
        return data.stream().filter(tester).map(mapper)
                .collect(Collectors.toList());
    }

    private <X, Y> List<Y> collectSuitableItemsToCollection(final List<X> data,
                                                final Predicate<X> tester,
                                                final Function<X, Y> mapper) {
        return data.stream().filter(tester).map(mapper)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private <X, Y> List<Y> collectSuitableItemsWithGrouping(final List<X> data,
                                                final Predicate<X> tester,
                                                final Function<X, Y> mapper) {
        return new ArrayList<>(data.stream().filter(tester)
                .collect(Collectors.groupingBy(mapper)).keySet());
    }

    private <X, Y> List<Y> collectSuitableItemsWithMapping(final List<X> data,
                                                final Predicate<X> tester,
                                                final Function<X, Y> mapper) {
        return data.stream().filter(tester)
                .collect(Collectors.mapping(mapper, Collectors.toList()));
    }

    private <X> long countUpSuitableItems(final List<X> data, final Predicate<X> tester) {
        return data.stream().filter(tester)
                .count();
    }

    private int calculateAgeSum(final List<Person> persons,
                                   final Predicate<Person> tester) {
        return persons.stream().filter(tester).mapToInt(Person::getAge)
                .sum();
    }

    private int calculateAgeSumWithReducing(final List<Person> persons,
                                   final Predicate<Person> tester) {
        return persons.stream().filter(tester).map(Person::getAge)
                .reduce(0, Integer::sum);
    }

    private int calculateAgeSumWithReducingCollector(final List<Person> persons,
                                                     final Predicate<Person> tester) {
        return persons.stream().filter(tester)
                .collect(Collectors.reducing(0, Person::getAge, Integer::sum));
    }

    private int calculateAgeSumWithSummingCollector(final List<Person> persons,
                                                     final Predicate<Person> tester) {
        return persons.stream().filter(tester)
                .collect(Collectors.summingInt(Person::getAge));
    }

    private double calculateAgeAvg(final List<Person> persons,
                                   final Predicate<Person> tester) {
        return persons.stream().filter(tester).mapToInt(Person::getAge)
                .average().orElse(0.0);
    }

    private double calculateAgeAvgWithCollector(final List<Person> persons,
                                                final Predicate<Person> tester) {
        return persons.stream().filter(tester).map(Person::getAge)
                .collect(Averager::new, Averager::accept, Averager::combine)
                .average().orElse(0.0);
    }

    private double calculateAgeAvgWithAveragingCollector(final List<Person> persons,
                                   final Predicate<Person> tester) {
        return persons.stream().filter(tester)
                .collect(Collectors.averagingInt(Person::getAge));
    }

    private static class Averager implements IntConsumer {

        private int total = 0;
        private int count = 0;

        @Override
        public void accept(int value) {
            total += value;
            count++;
        }

        boolean isPresent() {
            return count > 0;
        }

        OptionalDouble average() {
            if (isPresent()) {
                return OptionalDouble.of((double) total / count);
            } else {
                return OptionalDouble.empty();
            }
        }

        void combine(final Averager other) {
            total += other.total;
            count += other.count;
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

    @SuppressWarnings("Convert2MethodRef")
    private List<String> collectNamesWithLoop() {
        final List<String> names = new ArrayList<>();
        processData(PERSONS,
                person -> !person.getName().startsWith("_"),
                person -> person.getName(),
                name -> names.add(name) );
        return names;
    }

    private List<String> collectNamesWithForEach() {
        final List<String> names = new ArrayList<>();
        processDataInStream(PERSONS,
                person -> !person.getName().startsWith("_"),
                Person::getName,
                names::add);
        return names;
    }

    private List<String> collectNamesWithListCollector() {
        return collectSuitableItemsToList(PERSONS,
                person -> !person.getName().startsWith("_"),
                Person::getName);
    }

    private List<String> collectNamesWithCustomCollector() {
        return collectSuitableItemsToCollection(PERSONS,
                person -> !person.getName().startsWith("_"),
                Person::getName);
    }

    private List<String> collectNamesWithGroupingCollector() {
        return collectSuitableItemsWithGrouping(PERSONS,
                person -> !person.getName().startsWith("_"),
                Person::getName);
    }

    private List<String> collectNamesWithMappingCollector() {
        return collectSuitableItemsWithMapping(PERSONS,
                person -> !person.getName().startsWith("_"),
                Person::getName);
    }

    @Test
    public void shouldCollectNamesEqually() {
        final List<String> names = collectNamesWithLoop();
        assertTrue(names.equals(collectNamesWithForEach()));
        assertTrue(names.equals(collectNamesWithListCollector()));
        assertTrue(names.equals(collectNamesWithCustomCollector()));
        assertTrue(names.equals(collectNamesWithGroupingCollector()));
        assertTrue(names.equals(collectNamesWithMappingCollector()));
    }

    @Test
    public void shouldCalculateAgeSumEqually() {
        final Predicate<Person> tester = person -> !person.getName().startsWith("_");
        assertEquals(calculateAgeSum(PERSONS, tester),
                calculateAgeSumWithReducing(PERSONS, tester));
    }

    @Test
    public void shouldCalculateAgeAvg() {
        final Predicate<Person> tester = person -> !person.getName().startsWith("_");
        final long numberOfPersons = countUpSuitableItems(PERSONS, tester);
        final double ageAvg = calculateAgeAvg(PERSONS, tester);
        final double delta = ageAvg * 0.001;
        assertEquals(ageAvg,
                (double) calculateAgeSum(PERSONS, tester) / numberOfPersons,
                delta);
        assertEquals(ageAvg,
                (double) calculateAgeSumWithReducing(PERSONS, tester) / numberOfPersons,
                delta);
        assertEquals(ageAvg,
                (double) calculateAgeSumWithReducingCollector(PERSONS, tester) / numberOfPersons,
                delta);
        assertEquals(ageAvg,
                (double) calculateAgeSumWithSummingCollector(PERSONS, tester) / numberOfPersons,
                delta);
    }

    @Test
    public void shouldCalculateAgeAvgEqually() {
        final Predicate<Person> tester = person -> !person.getName().startsWith("_");
        final double ageAvg = calculateAgeAvg(PERSONS, tester);
        final double delta = ageAvg * 0.001;
        assertEquals(ageAvg, calculateAgeAvgWithCollector(PERSONS, tester), delta);
        assertEquals(ageAvg, calculateAgeAvgWithAveragingCollector(PERSONS, tester), delta);
    }
}