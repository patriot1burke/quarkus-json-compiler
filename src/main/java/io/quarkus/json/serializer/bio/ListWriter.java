package io.quarkus.json.serializer.bio;

import java.util.List;

public class ListWriter implements ObjectWriter {
    private ObjectWriter elementWriter;

    public ListWriter(ObjectWriter elementWriter) {
        this.elementWriter = elementWriter;
    }

    @Override
    public void write(JsonWriter writer, Object target) {
        List list = (List)target;
    }
}
