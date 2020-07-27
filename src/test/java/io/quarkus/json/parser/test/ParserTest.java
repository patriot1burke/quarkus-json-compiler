package io.quarkus.json.parser.test;

import io.quarkus.json.parser.ParserContext;
import io.quarkus.json.parser.PersonParser;
import io.quarkus.json.parser.Person;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ParserTest {

    /**
     * skip [map, list, value]
     * map nonstring-key, object value, list value, generic
     * list value, object, generic
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
            "  \"dad\": {\n" +
            "    \"name\": \"John\",\n" +
            "    \"married\": true\n" +
            "  }\n" +
            "}";

    @Test
    public void testParser() {
        PersonParser p = new PersonParser();
        ParserContext ctx = p.parser();

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
        Assertions.assertTrue(person.getDad().isMarried());
    }
}
