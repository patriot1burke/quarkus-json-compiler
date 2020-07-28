package io.quarkus.json.parser;

import java.util.HashMap;
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
    public MapParser(ContextValue keyFunction, ContextValue valueFunction) {
        this.keyFunction = keyFunction;
        this.valueFunction = valueFunction;
        this.valueState = this::value;
    }

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
    }

    @Override
    public boolean handleKey(ParserContext ctx) {
        Object key = keyFunction.value(ctx);
        ctx.pushState((ctx1) -> {
            ctx1.popState();
            Object value = valueFunction.value(ctx1);
            Map map = ctx1.target();
            map.put(key, value);
        });
        ctx.pushState(valueState);
        return true;
    }

}
