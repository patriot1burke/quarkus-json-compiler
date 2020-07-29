package io.quarkus.json.parser;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    // we do these get methods to avoid object creations
    // as method references create a new object every time
    private ParserState start  = this::start;
    @Override
    public ParserState getStart() {
        return start;
    }
    public void start(ParserContext ctx) {
        startObject(ctx);
    }

    @Override
    public void appendToken(ParserContext ctx, char c) {
        ctx.token().append(c);
    }
}
