package io.quarkus.json.deserializer.buffered;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    public void start(ParserContext ctx) {
        startObject(ctx);
    }

}
