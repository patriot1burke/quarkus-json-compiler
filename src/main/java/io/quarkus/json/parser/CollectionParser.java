package io.quarkus.json.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionParser extends ObjectParser {
    ContextValue valueFunction;
    ParserState  valueState;
    private ParserState addListValue = this::addListValue;

    public CollectionParser(ContextValue valueFunction, ParserState valueState) {
        this.valueFunction = valueFunction;
        this.valueState = valueState;
    }

    @Override
    public void start(ParserContext ctx) {
        startList(ctx);
    }

    public void addListValue(ParserContext ctx) {
        Object value = valueFunction.value(ctx);
        Collection collection = ctx.target();
        collection.add(value);
    }

    @Override
    public void listValue(ParserContext ctx) {
        valueState.parse(ctx);
    }

}
