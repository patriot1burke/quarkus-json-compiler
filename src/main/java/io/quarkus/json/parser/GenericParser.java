package io.quarkus.json.parser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericParser extends SkipParser implements JsonParser {

    public static final GenericParser PARSER = new GenericParser();

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext();
        ctx.pushState(this::value);
        return ctx;
    }

    @Override
    public void appendToken(ParserContext ctx, char c) {
        ctx.token().append(c);
    }

    @Override
    public void beginList(ParserContext ctx) {
        ctx.pushTarget(new LinkedList());
    }
    // we do these get methods to avoid object creations
    // as method references create a new object every time
    private ParserState addListValue  = this::addListValue;
    @Override
    public ParserState getAddListValue() {
        return addListValue;
    }

    @Override
    public void addListValue(ParserContext ctx) {
        ctx.popState();
        Object val = ctx.popTarget();
        List list = ctx.target();
        list.add(val);
    }

    @Override
    public boolean handleKey(ParserContext ctx) {
        String key = ctx.popToken();
        ctx.pushState((ctx1) -> {
            ctx1.popState();
            Object val = ctx1.popTarget();
            Map map = ctx1.target();
            map.put(key, val);
        });
        ctx.pushState(this::value);
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
        ctx.pushTarget(Integer.valueOf(ctx.popToken()));
    }

    @Override
    public void endFloatValue(ParserContext ctx) {
        ctx.pushTarget(Float.valueOf(ctx.popToken()));
    }

    @Override
    public void endBooleanValue(ParserContext ctx) {
        ctx.pushTarget(Boolean.valueOf(ctx.popToken()));
    }
}
