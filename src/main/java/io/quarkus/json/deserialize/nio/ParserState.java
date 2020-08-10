package io.quarkus.json.deserialize.nio;

public interface ParserState {
    boolean parse(ParserContext ctx);
}
