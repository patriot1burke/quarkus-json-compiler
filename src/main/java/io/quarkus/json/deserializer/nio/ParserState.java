package io.quarkus.json.deserializer.nio;

public interface ParserState {
    boolean parse(ParserContext ctx);
}
