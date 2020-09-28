package io.quarkus.json.serializer.bio;

import java.util.Collection;
import java.util.Map;

public class MapWriter implements ObjectWriter {
    private ObjectWriter valueWriter;

    public MapWriter(ObjectWriter valueWriter) {
        this.valueWriter = valueWriter;
    }

    @Override
    public void write(JsonWriter writer, Object target) {
        Map map = (Map)target;
        writer.write(map, valueWriter);
    }
}
