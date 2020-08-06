package io.quarkus.json.nio;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericParser extends SkipParser implements JsonParser {

    public static final GenericParser PARSER = new GenericParser();

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext(this::value);
        return ctx;
    }

    @Override
    public void beginList(ParserContext ctx) {
        ctx.pushTarget(new LinkedList());
    }

    @Override
    public void addListValue(ParserContext ctx) {
        Object val = ctx.popTarget();
        List list = ctx.target();
        list.add(val);
    }

    @Override
    public boolean key(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) {
            ctx.pushState(getContinueKey());
            return false;
        }
        ctx.endToken();
        String key = ctx.popToken();
        int stateIndex = ctx.stateIndex();
        if (!valueSeparator(ctx)) {
            ctx.pushState((ctx1) -> fillKey(ctx1, key), stateIndex);
            ctx.pushState(getContinueValue(), stateIndex);
        }
        if (!value(ctx)) {
            ctx.pushState((ctx1) -> fillKey(ctx1, key), stateIndex);
        }
        return fillKey(ctx, key);
    }

    public boolean fillKey(ParserContext ctx, String key) {
        Object val = ctx.popTarget();
        Map map = ctx.target();
        map.put(key, val);

        return true;
    }

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
    }

    @Override
    public void endStringValue(ParserContext ctx) {
        ctx.pushTarget(ctx.popToken());
    }

    @Override
    public void endNumberValue(ParserContext ctx) {
        ctx.pushTarget(ctx.popLongToken());
    }

    @Override
    public void endFloatValue(ParserContext ctx) {
        ctx.pushTarget(Float.valueOf(ctx.popToken()));
    }

    @Override
    public void endBooleanValue(ParserContext ctx) {
        ctx.pushTarget(ctx.popBooleanToken());
    }
}
