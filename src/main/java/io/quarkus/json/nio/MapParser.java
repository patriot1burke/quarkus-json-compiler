package io.quarkus.json.nio;

import java.util.Map;

public class MapParser extends ObjectParser {
    ContextValue keyFunction;
    ContextValue valueFunction;
    ParserState valueState;

    public MapParser(ContextValue keyFunction, ContextValue valueFunction, ParserState valueState) {
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction;
        this.valueState = valueState;
    }

    @Override
    public boolean key(ParserContext ctx) {
        ctx.startToken(0);
        int c = ctx.skipToQuote();
        if (c == 0) {
            ctx.pushState(getContinueKey());
            return false;
        }
        ctx.endToken();
        Object key = keyFunction.value(ctx);
        int stateIndex = ctx.stateIndex();
        if (!valueSeparator(ctx)) {
            ctx.pushState((ctx1) -> fillKey(ctx, key), stateIndex);
            ctx.pushState(getContinueValue(), stateIndex);
        }
        if (!valueState.parse(ctx)) {
            ctx.pushState((ctx1) -> fillKey(ctx1, key), stateIndex);
        }
        return fillKey(ctx, key);
    }

    public boolean fillKey(ParserContext ctx, Object key) {
        Object value = valueFunction.value(ctx);
        Map map = ctx.target();
        map.put(key, value);

        return true;
    }
}
