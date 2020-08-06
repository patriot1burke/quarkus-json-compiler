package io.quarkus.json.parser;

import java.util.Map;

public class MapParser extends ObjectParser {
    ContextValue keyFunction;
    ContextValue valueFunction;
    ParserState  valueState;

    public MapParser(ContextValue keyFunction, ContextValue valueFunction, ParserState valueState) {
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction;
        this.valueState = valueState;
    }

    @Override
    public void key(ParserContext ctx) {
        ctx.startToken(0);
        ctx.skipToQuote();
        ctx.endToken();
        Object key = keyFunction.value(ctx);
        valueSeparator(ctx);
        valueState.parse(ctx);
        Object value = valueFunction.value(ctx);
        Map map = ctx.target();
        map.put(key, value);

    }
}
