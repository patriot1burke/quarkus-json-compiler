package io.quarkus.json.parser.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import io.quarkus.json.generator.Compile;
import io.quarkus.json.generator.CompileOptions;
import io.quarkus.json.generator.DeserializerGenerator;
import io.quarkus.json.generator.ObjectDeserializer;
import io.quarkus.json.parser.JsonParser;
import io.quarkus.json.parser.ParserContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DeserializerTest {

    static Class<?> deserializerClass;

    @BeforeAll
    public static void generateDeserializer() {
        deserializerClass = compile(Person.class);
    }

    public static Class<?> compile(Class<?> targetClass) {
        long start = System.currentTimeMillis();
        try {
            Map<Class, String> codeMap = new ObjectDeserializer().deserializerFor(targetClass);
            System.out.println("---- Deserializer generator took: " + (System.currentTimeMillis() - start) + " (ms)");

            Assertions.assertEquals(1, codeMap.size());
            Assertions.assertTrue(codeMap.containsKey(targetClass));

            String code = codeMap.get(targetClass);

            String classname = DeserializerGenerator.fqn(targetClass, targetClass);
            return Compile.compileClass(classname, code, new CompileOptions());
        } finally {
            System.out.println("---- Compilation took: " + (System.currentTimeMillis() - start) + " (ms)");
        }
    }

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
    public void testParser() throws Exception {
        JsonParser parser = (JsonParser)deserializerClass.newInstance();
        ParserContext ctx = parser.parser();

        Person person = ctx.parse(json);
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

    @Test
    public void testVsJackson() throws Exception {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
        ObjectReader reader = mapper.readerFor(Person.class);
        JsonParser parser = (JsonParser)deserializerClass.newInstance();

        // warm up
        for (int i = 0; i < 10; i++) {
            reader.readValue(json);
            parser.parser().parse(json);
        }

        long start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            reader.readValue(json);
        }
        System.out.println("Jackson took: " + (System.currentTimeMillis() - start) + " (ms)");

        start = System.currentTimeMillis();
        for (int i = 0; i < 1000; i++) {
            parser.parser().parse(json);
        }
        System.out.println("Generator took: " + (System.currentTimeMillis() - start) + " (ms)");
    }

    @Test
    public void testProfile() throws Exception {
        JsonParser parser = (JsonParser)deserializerClass.newInstance();
        for (int i = 0; i < 10000; i++) {
            parser.parser().parse(json);
        }

    }


}
