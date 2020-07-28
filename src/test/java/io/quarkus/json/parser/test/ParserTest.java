package io.quarkus.json.parser.test;

import io.quarkus.json.parser.GenericParser;
import io.quarkus.json.parser.JsonParser;
import io.quarkus.json.parser.ParserContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

public class ParserTest {

    /**
     * skip [map, list, value]
     * map nonstring-key, object value, list value
     * list value, object
     */

    static String json = "{\n" +
            "  \"intMap\": {\n" +
            "    \"one\": 1,\n" +
            "    \"two\": 2\n" +
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

    @Test
    public void testParser() {
        ParserContext ctx = PersonParser.PARSER.parser();

        for (char c : json.toCharArray()) {
            System.out.write(c);
            ctx.parse(c);
        }

        Person person = ctx.target();
        Assertions.assertEquals("Bill", person.getName());
        Assertions.assertEquals(50, person.getAge());
        Assertions.assertTrue(person.isMarried());
        Assertions.assertEquals(123.23F, person.getMoney());
        Assertions.assertEquals(1, person.getIntMap().get("one"));
        Assertions.assertEquals(2, person.getIntMap().get("two"));
        Assertions.assertEquals("John", person.getDad().getName());
        Assertions.assertEquals("Sammy", person.getKids().get("Sammy").getName());
        Assertions.assertEquals(6, person.getKids().get("Sammy").getAge());
        Assertions.assertEquals("Suzi", person.getKids().get("Suzi").getName());
        Assertions.assertEquals(7, person.getKids().get("Suzi").getAge());
        Assertions.assertEquals("Ritchie", person.getSiblings().get(0).getName());
        Assertions.assertEquals("Joani", person.getSiblings().get(1).getName());
        Assertions.assertTrue(person.getDad().isMarried());
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
        JsonParser p = GenericParser.PARSER;
        ParserContext ctx = p.parser();

        for (char c : generic.toCharArray()) {
            System.out.write(c);
            ctx.parse(c);
        }

        Map person = ctx.target();
        Assertions.assertEquals("Bill", person.get("name"));
        Assertions.assertEquals(50, person.get("age"));
        Assertions.assertEquals(true, person.get("married"));
        Assertions.assertEquals(123.23F, person.get("money"));
        Assertions.assertEquals(1, ((Map)person.get("intMap")).get("one"));
        Assertions.assertEquals(2, ((Map)person.get("intMap")).get("two"));
        Assertions.assertEquals("John", ((Map)person.get("dad")).get("name"));
        Assertions.assertEquals(true, ((Map)person.get("dad")).get("married"));
        List list = (List)person.get("list");
        Assertions.assertEquals("one", list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3.0F, list.get(2));
        Assertions.assertEquals(true, list.get(3));
        Assertions.assertEquals("John", ((Map)list.get(4)).get("name"));
        Assertions.assertEquals(true, ((Map)list.get(4)).get("married"));
        List list2 = (List)person.get("list2");
        Assertions.assertEquals(0, list2.get(0));
        Assertions.assertEquals(1, list2.get(1));
        Assertions.assertEquals(2, list2.get(2));
        Assertions.assertEquals(3, list2.get(3));

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

        for (char c : genericList.toCharArray()) {
            System.out.write(c);
            ctx.parse(c);
        }

        List list = ctx.target();
        Assertions.assertEquals("one", list.get(0));
        Assertions.assertEquals(2, list.get(1));
        Assertions.assertEquals(3.0F, list.get(2));
        Assertions.assertEquals(true, list.get(3));
        Assertions.assertEquals("John", ((Map)list.get(4)).get("name"));
        Assertions.assertEquals(true, ((Map)list.get(4)).get("married"));

    }

}
