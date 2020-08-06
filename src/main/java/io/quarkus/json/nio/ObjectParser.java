package io.quarkus.json.nio;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    public boolean start(ParserContext ctx) {
        return startObject(ctx);
    }

}
