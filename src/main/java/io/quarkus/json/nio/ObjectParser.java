package io.quarkus.json.nio;

public class ObjectParser extends SkipParser {
    public static final ObjectParser PARSER = new ObjectParser();

    public boolean start(ParserContext ctx) {
        return startObject(ctx);
    }

    @Override
    public void startToken(ParserContext ctx) {
        ctx.startToken();
    }

    @Override
    public void startTokenNextConsumed(ParserContext ctx) {
        ctx.startTokenNextConsumed();
    }

    @Override
    public void endToken(ParserContext ctx) {
        ctx.endToken();
    }


}
