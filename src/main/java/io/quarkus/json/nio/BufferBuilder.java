package io.quarkus.json.nio;

import java.io.ByteArrayOutputStream;

public class BufferBuilder extends ByteArrayOutputStream {

    public BufferBuilder() {
    }

    public BufferBuilder(int size) {
        super(size);
    }

    public byte[] getBuffer() {
        return buf;
    }
}
