package io.quarkus.json.deserialize.buffered;

public interface ParserState {
    void parse(ParserContext ctx);
}
