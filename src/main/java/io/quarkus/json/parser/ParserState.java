package io.quarkus.json.parser;

@FunctionalInterface
interface ParserState {
    void parse(char c, ParserContext ctx);
}
