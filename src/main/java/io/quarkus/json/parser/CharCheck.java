package io.quarkus.json.parser;

public class CharCheck {
    public static boolean isWhitespace(char c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }


}
