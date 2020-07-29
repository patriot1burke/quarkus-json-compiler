package io.quarkus.json.parser;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    public void start(ParserContext ctx) {
        startObject(ctx);
    }

    @Override
    public void appendToken(ParserContext ctx, char c) {
        ctx.token().append(c);
    }
}
