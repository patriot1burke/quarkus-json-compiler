package io.quarkus.json.serializer.bio;

public interface ByteWriter {
    void write(int b);
    void write(byte[] bytes);
}
