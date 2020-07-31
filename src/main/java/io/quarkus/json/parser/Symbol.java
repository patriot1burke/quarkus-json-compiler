package io.quarkus.json.parser;

import java.io.UnsupportedEncodingException;

final public class Symbol {
    final String value;
    final byte[] utf8;

    public Symbol(String value) {
        this.value = value;
        try {
            this.utf8 = value.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    final public String getValue() {
        return value;
    }

    final public byte[] getUtf8() {
        return utf8;
    }
}
