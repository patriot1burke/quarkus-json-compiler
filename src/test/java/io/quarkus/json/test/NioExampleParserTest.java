package io.quarkus.json.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.quarkus.json.deserializer.nio.GenericParser;
import io.quarkus.json.deserializer.nio.JsonParser;
import io.quarkus.json.deserializer.nio.ParserContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NioExampleParserTest {

    /**
     * map nonstring-key, object value, list value
     * list value, object
     */

    static String json = "{\n" +
            "  \"intMap\": {\n" +
            "    \"one\": 1,\n" +
            "    \"two\": 2\n" +
            "  },\n" +
            "  \"genericMap\": {\n" +
            "    \"three\": 3,\n" +
            "    \"four\": 4\n" +
            "  },\n" +
            "  \"name\": \"Bill\",\n" +
            "  \"age\": 50,\n" +
            "  \"money\": 123.23,\n" +
            "  \"married\": true,\n" +
            "  \"junkInt\": 666,\n" +
            "  \"pets\": [ \"itchy\", \"scratchy\"],\n" +
            "  \"junkFloat\": 6.66,\n" +
            "  \"kids\": {\n" +
            "    \"Sammy\": {\n" +
            "      \"name\": \"Sammy\",\n" +
            "      \"age\": 6\n" +
            "    },\n" +
            "    \"Suzi\": {\n" +
            "      \"name\": \"Suzi\",\n" +
            "      \"age\": 7\n" +
            "    }\n" +
            "  },\n" +
            "  \"siblings\": [\n" +
            "    {\n" +
            "      \"name\": \"Ritchie\"\n" +
            "    },\n" +
            "    {\n" +
            "      \"name\": \"Joani\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"junkList\": [\"1\", \"2\"]," +
            "  \"junkBool\": true,\n" +
            "  \"junkMap\": {\n" +
            "    \"foo\": \"bar\",\n" +
            "    \"one\": 1,\n" +
            "    \"list\": [1, 2, 3, 4]\n" +
            "  },\n" +
            "  \"dad\": {\n" +
            "    \"name\": \"John\",\n" +
            "    \"married\": true\n" +
            "  }\n" +
            "}";


    static String arrayOnly = "{\n" +
            "  \"pets\": [ \"itchy\", \"scratchy\"]\n" +
            "}";
    @Test
    public void testNioArrayOnly() {
        List<String> breakup = breakup(arrayOnly, 1);
        ParserContext ctx = NioPersonParser.PARSER.parser();
        for (String str : breakup) {
            if (ctx.parse(str)) break;
        }
        Person person = ctx.target();
        Assertions.assertTrue(person.getPets().contains("itchy"));
        Assertions.assertTrue(person.getPets().contains("scratchy"));

    }

    static String kidsOnly = "{\n" +
            "  \"kids\": {\n" +
            "    \"Sammy\": {\n" +
            "      \"name\": \"Sammy\",\n" +
            "      \"age\": 6\n" +
            "    },\n" +
            "    \"Suzi\": {\n" +
            "      \"name\": \"Suzi\",\n" +
            "      \"age\": 7\n" +
            "    }\n" +
            "  }\n" +
            "}";

    @Test
    public void testNioMapObjectOnly() {
        List<String> breakup = breakup(kidsOnly, 1);
        ParserContext ctx = NioPersonParser.PARSER.parser();
        for (String str : breakup) {
            if (ctx.parse(str)) break;
        }
        Person person = ctx.target();
        Assertions.assertEquals("Sammy", person.getKids().get("Sammy").getName());
        Assertions.assertEquals(6, person.getKids().get("Sammy").getAge());
        Assertions.assertEquals("Suzi", person.getKids().get("Suzi").getName());
        Assertions.assertEquals(7, person.getKids().get("Suzi").getAge());

    }

    static String dadOnly = "{\n" +
            "  \"dad\": {\n" +
            "    \"name\": \"John\",\n" +
            "    \"married\": true\n" +
            "  }\n" +
            "}";

    @Test
    public void testNioObjectOnly() {
        List<String> breakup = breakup(dadOnly, 1);
        ParserContext ctx = NioPersonParser.PARSER.parser();
        for (String str : breakup) {
            if (ctx.parse(str)) break;
        }
        Person person = ctx.target();
        Assertions.assertEquals("John", person.getDad().getName());
        Assertions.assertTrue(person.getDad().isMarried());

    }



    @Test
    public void testParser() {
        for (int i = 1; i <= json.length(); i++) {
            System.out.println("Buffer size: " + i);
            List<String> breakup = breakup(json, i);
            ParserContext ctx = NioPersonParser.PARSER.parser();
            for (String str : breakup) {
                if (ctx.parse(str)) break;
            }
            Person person = ctx.target();
            validatePerson(person);

        }
        ParserContext ctx = NioPersonParser.PARSER.parser();
        Assertions.assertTrue(ctx.parse(json));
        Person person = ctx.target();
        validatePerson(person);
    }

    @Test
    public void testNioParser() {
        List<String> breakup = breakup(json, 7);
        ParserContext ctx = NioPersonParser.PARSER.parser();
        for (String str : breakup) {
            if (ctx.parse(str)) break;
        }
        System.out.println();
        Person person = ctx.target();
        validatePerson(person);

    }

    List<String> breakup(String str, int size) {
        List<String> breakup = new LinkedList<>();
        int i = 0;
        int len = str.length();
        while (true) {
            if (size > len - i) {
                breakup.add(str.substring(i));
                return breakup;
            }
            breakup.add(str.substring(i, i + size));
            i += size;
        }
    }

    public void validatePerson(Person person) {
        Assertions.assertEquals("Bill", person.getName());
        Assertions.assertEquals(50, person.getAge());
        Assertions.assertTrue(person.isMarried());
        Assertions.assertEquals(123.23F, person.getMoney());
        Assertions.assertEquals(1, person.getIntMap().get("one"));
        Assertions.assertEquals(2, person.getIntMap().get("two"));
        Assertions.assertEquals(3l, person.getGenericMap().get("three"));
        Assertions.assertEquals(4l, person.getGenericMap().get("four"));
        Assertions.assertEquals("John", person.getDad().getName());
        Assertions.assertTrue(person.getDad().isMarried());
        Assertions.assertEquals("Sammy", person.getKids().get("Sammy").getName());
        Assertions.assertEquals(6, person.getKids().get("Sammy").getAge());
        Assertions.assertEquals("Suzi", person.getKids().get("Suzi").getName());
        Assertions.assertEquals(7, person.getKids().get("Suzi").getAge());
        Assertions.assertEquals("Ritchie", person.getSiblings().get(0).getName());
        Assertions.assertEquals("Joani", person.getSiblings().get(1).getName());
        Assertions.assertTrue(person.getPets().contains("itchy"));
        Assertions.assertTrue(person.getPets().contains("scratchy"));
    }

    static String generic = "{\n" +
            "  \"intMap\": {\n" +
            "    \"one\": 1,\n" +
            "    \"two\": 2\n" +
            "  },\n" +
            "  \"name\": \"Bill\",\n" +
            "  \"age\": 50,\n" +
            "  \"money\": 123.23,\n" +
            "  \"married\": true,\n" +
            "  \"list\": [\n" +
            "    \"one\",\n" +
            "    2,\n" +
            "    3.0,\n" +
            "    true,\n" +
            "    {\n" +
            "      \"name\": \"John\",\n" +
            "      \"married\": true\n" +
            "    }\n" +
            "  ],\n" +
            "  \"list2\": [0, 1, 2, 3   ]   ,\n" +
            "  \"dad\": {\n" +
            "    \"name\": \"John\",\n" +
            "    \"married\": true\n" +
            "  }\n" +
            "}";



    @Test
    public void testGenericParser() {
        for (int i = 1; i <= generic.length(); i++) {
            System.out.println("Buffer size: " + i);
            List<String> breakup = breakup(generic, i);
            ParserContext ctx = GenericParser.PARSER.parser();
            for (String str : breakup) {
                //System.out.println(str);
                if (ctx.parse(str)) break;
            }
            validateGeneric(ctx);

        }

        JsonParser p = GenericParser.PARSER;
        ParserContext ctx = p.parser();
        Assertions.assertTrue(ctx.parse(generic));
        validateGeneric(ctx);

    }

    public void validateGeneric(ParserContext ctx) {
        Map person = ctx.target();
        Assertions.assertEquals("Bill", person.get("name"));
        Assertions.assertEquals(50L, person.get("age"));
        Assertions.assertEquals(true, person.get("married"));
        Assertions.assertEquals(123.23F, person.get("money"));
        Assertions.assertEquals(1L, ((Map)person.get("intMap")).get("one"));
        Assertions.assertEquals(2L, ((Map)person.get("intMap")).get("two"));
        Assertions.assertEquals("John", ((Map)person.get("dad")).get("name"));
        Assertions.assertEquals(true, ((Map)person.get("dad")).get("married"));
        List list = (List)person.get("list");
        Assertions.assertEquals("one", list.get(0));
        Assertions.assertEquals(2L, list.get(1));
        Assertions.assertEquals(3.0F, list.get(2));
        Assertions.assertEquals(true, list.get(3));
        Assertions.assertEquals("John", ((Map)list.get(4)).get("name"));
        Assertions.assertEquals(true, ((Map)list.get(4)).get("married"));
        List list2 = (List)person.get("list2");
        Assertions.assertEquals(0L, list2.get(0));
        Assertions.assertEquals(1L, list2.get(1));
        Assertions.assertEquals(2L, list2.get(2));
        Assertions.assertEquals(3L, list2.get(3));
    }

    static String genericList = "[\n" +
            "  \"one\",\n" +
            "  2,\n" +
            "  3.0,\n" +
            "  true,\n" +
            "  {\n" +
            "    \"name\": \"John\",\n" +
            "    \"married\": true\n" +
            "  }\n" +
            "]\n";


    @Test
    public void testGenericList() {
        JsonParser p = GenericParser.PARSER;
        ParserContext ctx = p.parser();
        Assertions.assertTrue(ctx.parse(genericList));

        List list = ctx.target();
        Assertions.assertEquals("one", list.get(0));
        Assertions.assertEquals(2L, list.get(1));
        Assertions.assertEquals(3.0F, list.get(2));
        Assertions.assertEquals(true, list.get(3));
        Assertions.assertEquals("John", ((Map)list.get(4)).get("name"));
        Assertions.assertEquals(true, ((Map)list.get(4)).get("married"));

    }

    @Test
    public void testVsJackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        ObjectReader reader = mapper.readerFor(Person.class);
        JsonParser parser = NioPersonParser.PARSER;
        io.quarkus.json.deserializer.buffered.JsonParser buffered = BufferedPersonParser.PARSER;
        byte[] array = json.getBytes("UTF-8");
        // warm up
        int ITERATIONS = 10000;
        for (int i = 0; i < ITERATIONS; i++) {
            reader.readValue(array);
            parser.parser().parse(array);
            buffered.parser().parse(array);
        }
        long start = 0;
        System.gc();
        Thread.sleep(100);

        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            buffered.parser().parse(array);
        }
        System.out.println("buffered took: " + (System.currentTimeMillis() - start) + " (ms)");

        System.gc();
        Thread.sleep(100);

        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            parser.parser().parse(array);
        }
        System.out.println("Nio took: " + (System.currentTimeMillis() - start) + " (ms)");

        System.gc();
        Thread.sleep(100);


        start = System.currentTimeMillis();
        for (int i = 0; i < ITERATIONS; i++) {
            reader.readValue(array);
        }
        System.out.println("Jackson took: " + (System.currentTimeMillis() - start) + " (ms)");
    }
}
