package io.quarkus.json.parser;

import java.util.HashMap;

abstract public class MapParser extends ObjectParser {

    @Override
    void handleKey(String property, ParserContext ctx) {
        ctx.state().push(this::valueSeparator);
        ctx.handler(this::mapStart);

    }

    public void mapStart(ParserContext ctx) {
        ctx.state().push(this::start);
    }

    @Override
    public void key(char c, ParserContext ctx) {
        if (c != '"') {
            ctx.token().append(c);
            return;
        }
        String property = ctx.popToken();
        ctx.state().pop();

        ctx.state().push((ch, ctx1) -> {
            if (Character.isWhitespace(c)) return;
            if (ch != ':') throw new RuntimeException("Expecting ':' at character " + ctx1.charCount());
            ctx1.state().pop();
            ctx1.state().push(this::valueStart);
            ctx1.handler((ctx2) -> {
                ctx2.handler((ctx3) -> {
                    setValue(property, ctx3.popToken(), ctx3);
                    ctx3.state().push(this::nextKey);
                });
            });
        });
    }

    abstract void setValue(String property, String value, ParserContext ctx);

}
