package io.quarkus.json.parser;

public class ByteArrayKey {
    private byte[] buffer;
    private int offset;
    private int length;
    private int hashCode;

    public ByteArrayKey(byte[] buffer, int offset, int length) {
        this.buffer = buffer;
        this.offset = offset;
        this.length = length;
        int result = 1;
        for (int i = 0; i < length; i++) {
            result = 31 * result + buffer[i + offset];
        }
        this.hashCode = result;
    }

    public ByteArrayKey(byte[] buffer) {
        this(buffer, 0, buffer.length);
    }

    @Override
    public boolean equals(Object o) {
        ByteArrayKey that = (ByteArrayKey) o;
        if (length != that.length || hashCode != that.hashCode) return false;
        int length = that.length;
        int thatOffset = that.offset;
        int thisOffset = this.offset;
        byte[] thisBuffer = this.buffer;
        byte[] thatBuffer = that.buffer;
        for (int i = 0; i < length; i++) {
            if (thisBuffer[thisOffset + i] != thatBuffer[thatOffset + i]) return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }
}
