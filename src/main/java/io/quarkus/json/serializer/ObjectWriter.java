package io.quarkus.json.serializer;

public interface ObjectWriter {
    void write(JsonWriter writer, Object target);
}
