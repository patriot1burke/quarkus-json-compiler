package io.quarkus.json.parser;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    public void start(ParserContext ctx) {
        startObject(ctx);
    }

}
