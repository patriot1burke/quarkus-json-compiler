package io.quarkus.json.test;

import io.quarkus.gizmo.TestClassLoader;
import io.quarkus.json.deserializer.nio.JsonParser;
import io.quarkus.json.deserializer.nio.ParserContext;
import io.quarkus.json.generator.nio.Deserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class NioDeserializerOutputTest {

    @Test
    public void testDeserializer() throws Exception {
        Deserializer.create(Simple.class).output(new TestClassOutput()).generate();
        Deserializer.create(Single.class).output(new TestClassOutput()).generate();
    }

    static String simpleJson = "{\n" +
            "  \"name\": 1,\n" +
            "  \"age\" : 2,\n" +
            "  \"money\": 3,\n" +
            "  \"married\": 4,\n" +
            "  \"q\": 5,\n" +
            "  \"qq\": 6,\n" +
            "  \"qqq\": 7\n" +
            "}\n";
    @Test
    public void testSingle() throws Exception {
        TestClassLoader loader = new TestClassLoader(Single.class.getClassLoader());
        Deserializer.create(Single.class).output(loader).generate();

        Class deserializer = loader.loadClass(Deserializer.fqn(Single.class, Single.class));
        JsonParser parser = (JsonParser)deserializer.newInstance();
        ParserContext ctx = parser.parser();
        Assertions.assertTrue(ctx.parse(simpleJson));
        Single single = ctx.popTarget();
        Assertions.assertEquals(1, single.getName());
    }
    @Test
    public void testSimple() throws Exception {
        TestClassLoader loader = new TestClassLoader(Single.class.getClassLoader());
        Deserializer.create(Simple.class).output(loader).generate();

        Class deserializer = loader.loadClass(Deserializer.fqn(Simple.class, Simple.class));
        JsonParser parser = (JsonParser)deserializer.newInstance();
        ParserContext ctx = parser.parser();
        Assertions.assertTrue(ctx.parse(simpleJson));
        Simple simple = ctx.popTarget();
        Assertions.assertEquals(1, simple.getName());
        Assertions.assertEquals(2, simple.getAge());
        Assertions.assertEquals(3, simple.getMoney());
        Assertions.assertEquals(4, simple.getMarried());
        Assertions.assertEquals(5, simple.getQ());
        Assertions.assertEquals(6, simple.getQq());
        Assertions.assertEquals(7, simple.getQqq());


    }

}
