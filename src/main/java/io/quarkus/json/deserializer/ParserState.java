package io.quarkus.json.deserializer;

public interface ParserState {
    boolean parse(ParserContext ctx);
}
