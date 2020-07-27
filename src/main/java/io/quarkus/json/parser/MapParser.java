package io.quarkus.json.parser;

abstract public class MapParser extends ObjectParser {

    @Override
    void handleKey(String property, ParserContext ctx) {
        throw new RuntimeException("Unreachable");
    }

    @Override
    public void key(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            ctx.token().append(c);
            return;
        }
        ctx.popState();
        ctx.pushState(this::nextKey);
        String property = ctx.popToken();
        ctx.pushState((ctx1) -> {
            ctx1.popState();
            setValue(property, ctx1.popToken(), ctx1);
        });
        ctx.pushState(this::valueStart);
        ctx.pushState(this::valueSeparator);
    }

    abstract void setValue(String property, String value, ParserContext ctx);

}
