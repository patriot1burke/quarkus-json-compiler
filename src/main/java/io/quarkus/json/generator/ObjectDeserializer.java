package io.quarkus.json.generator;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ObjectDeserializer {


    public Map<Class, String> deserializerFor(Class clz) {
        return deserializerFor(clz, clz);
    }


    public Map<Class, String> deserializerFor(Class clz, Type genericType) {
        Set<Class> need = new HashSet<>();
        Map<Class, String> generated = new HashMap<>();

        String generator = deserializerFor(clz, genericType, need);
        generated.put(clz, generator);

        while (!need.isEmpty()) {
            Iterator<Class> it = need.iterator();
            Class needed = it.next();
            if (generated.containsKey(needed)) {
                it.remove();
                continue;
            }

            generator = deserializerFor(needed, needed, need);
            generated.put(needed, generator);
        }
        return generated;
    }

    private String deserializerFor(Class clz, Type genericType, Set<Class> need) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        DeserializerGenerator generator = new DeserializerGenerator(writer, need, clz, genericType);
        generator.generate();
        writer.flush();
        return stringWriter.toString();
    }

}
