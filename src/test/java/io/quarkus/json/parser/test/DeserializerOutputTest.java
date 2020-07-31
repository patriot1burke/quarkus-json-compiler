package io.quarkus.json.parser.test;

import io.quarkus.json.generator.Compile;
import io.quarkus.json.generator.ObjectDeserializer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Map;

public class DeserializerOutputTest {

    @Test
    public void testOutput() {
        Map<Class, String> codeMap = new ObjectDeserializer().deserializerFor(Person.class);

        Assertions.assertEquals(1, codeMap.size());
        Assertions.assertTrue(codeMap.containsKey(Person.class));

        String code = codeMap.get(Person.class);
        Compile.printCode(code);
    }

}
