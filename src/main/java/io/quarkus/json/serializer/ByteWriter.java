package io.quarkus.json.serializer;

public interface ByteWriter {
    void write(int b);
    void write(byte[] bytes);
}
