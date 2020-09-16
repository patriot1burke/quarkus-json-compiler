package io.quarkus.json.deserializer.buffered;

public interface ParserState {
    void parse(ParserContext ctx);
}
