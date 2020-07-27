package io.quarkus.json.parser;

@FunctionalInterface
interface ParserHandler {
    void handle(ParserContext ctx);
}
