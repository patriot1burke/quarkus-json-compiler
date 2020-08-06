package io.quarkus.json.nio;

public interface ParserState {
    boolean parse(ParserContext ctx);
}
