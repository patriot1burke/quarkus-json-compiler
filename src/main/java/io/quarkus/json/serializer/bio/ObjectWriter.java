package io.quarkus.json.serializer.bio;

public interface ObjectWriter {
    void write(JsonWriter writer, Object target);
}
