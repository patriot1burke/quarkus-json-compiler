package io.quarkus.json.parser;

@FunctionalInterface
interface ParserState {
    void parse(ParserContext ctx);
}
