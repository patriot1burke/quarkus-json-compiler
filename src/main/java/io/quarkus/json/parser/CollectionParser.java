package io.quarkus.json.parser;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class CollectionParser extends ObjectParser {
    ContextValue valueFunction;
    ParserState  valueState;

    public CollectionParser(ContextValue valueFunction, ParserState valueState) {
        this.valueFunction = valueFunction;
        this.valueState = valueState;
    }
    public CollectionParser(ContextValue valueFunction) {
        this.valueFunction = valueFunction;
        this.valueState = this::value;
    }

    // we do these get methods to avoid object creations
    // as method references create a new object every time
    private ParserState start  = this::start;
    @Override
    public ParserState getStart() {
        return start;
    }
    private ParserState addListValue  = this::addListValue;
    @Override
    public ParserState getAddListValue() {
        return addListValue;
    }

    @Override
    public void start(ParserContext ctx) {
        startList(ctx);
    }

    public void addListValue(ParserContext ctx) {
        ctx.popState();
        Object value = valueFunction.value(ctx);
        Collection collection = ctx.target();
        collection.add(value);
    }

    @Override
    public void listValue(ParserContext ctx) {
        ctx.pushState(this::addListValue);
        ctx.pushState(valueState);
    }

}
