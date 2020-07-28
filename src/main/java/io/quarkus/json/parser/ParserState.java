package io.quarkus.json.parser;

interface ParserState {
    void parse(ParserContext ctx);
}
