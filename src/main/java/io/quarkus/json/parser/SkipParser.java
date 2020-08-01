package io.quarkus.json.parser;

import static io.quarkus.json.parser.IntChar.*;

public class SkipParser implements JsonParser {
    public static final SkipParser PARSER = new SkipParser();

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext(getStart());
        return ctx;
    }

    // we do these get methods to avoid object creations
    // as method references create a new object every time
    private ParserState start  = this::start;
    public ParserState getStart() {
        return start;
    }
    private ParserState nextKey  = this::nextKey;
    public ParserState getNextKey() {
        return nextKey;
    }
    private ParserState keyStart  = this::keyStart;
    public ParserState getKeyStart() {
        return keyStart;
    }
    private ParserState nextValue  = this::nextValue;
    public ParserState getNextValue() {
        return nextValue;
    }
    private ParserState stringValue  = this::stringValue;
    public ParserState getStringValue() {
        return stringValue;
    }
    private ParserState booleanValue  = this::booleanValue;
    public ParserState getBooleanValue() {
        return booleanValue;
    }
    private ParserState numberValue  = this::numberValue;
    public ParserState getNumberValue() {
        return numberValue;
    }
    private ParserState addListValue  = this::addListValue;
    public ParserState getAddListValue() {
        return addListValue;
    }
    private ParserState value  = this::value;
    public ParserState getValue() {
        return value;
    }
    private ParserState key  = this::key;
    public ParserState getKey() {
        return key;
    }
    private ParserState valueSeparator  = this::valueSeparator;
    public ParserState getValueSeparator() {
        return valueSeparator;
    }
    private ParserState floatValue  = this::floatValue;
    public ParserState getFloatValue() {
        return floatValue;
    }
    private ParserState integerValue  = this::integerValue;
    public ParserState getIntegerValue() {
        return integerValue;
    }
    private ParserState startStringValue  = this::startStringValue;
    public ParserState getStartStringValue() {
        return startStringValue;
    }
    private ParserState startNumberValue  = this::startNumberValue;
    public ParserState getStartNumberValue() {
        return startNumberValue;
    }
    private ParserState startBooleanValue  = this::startBooleanValue;
    public ParserState getStartBooleanValue() {
        return startBooleanValue;
    }
    private ParserState startIntegerValue  = this::startIntegerValue;
    public ParserState getStartIntegerValue() {
        return startIntegerValue;
    }

    public void start(ParserContext ctx) {
        value(ctx);
    }

    public void startList(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (c == INT_LBRACKET) {
            beginObject(ctx);
            ctx.pushState(getNextValue());
            listValue(ctx);
        } else {
            throw new RuntimeException("Expecting start of array");
        }

    }


    public void startStringValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            stringValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public void startBooleanValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (c== 't' || c == 'f') {
            ctx.startToken(-1);
            booleanValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public void value(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            stringValue(ctx);
        } else if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getNumberValue());
            ctx.startToken(-1);
            numberValue(ctx);
        } else if (c == INT_t || c == INT_f) {
            ctx.startToken(-1);
            booleanValue(ctx);
        } else if (c == INT_LCURLY) {
            beginObject(ctx);
            ctx.pushState(getNextKey());
            ctx.pushState(getKeyStart());
        } else if (c == INT_LBRACKET) {
            beginList(ctx);
            ctx.pushState(getNextValue());
            listValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public void unpushedValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            stringValue(ctx);
        } else if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getNumberValue());
            ctx.startToken(-1);
            numberValue(ctx);
        } else if (c == INT_t || c == INT_f) {
            ctx.startToken(-1);
            booleanValue(ctx);
        } else if (c == INT_LCURLY) {
            beginObject(ctx);
            ctx.pushState(getNextKey());
            ctx.pushState(getKeyStart());
        } else if (c == INT_LBRACKET) {
            beginList(ctx);
            ctx.pushState(getNextValue());
            listValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }



    public void beginList(ParserContext ctx) {

    }

    public void listValue(ParserContext ctx) {
        ctx.pushState(getAddListValue());
        ctx.pushState(getValue());
    }

    public void addListValue(ParserContext ctx) {
        ctx.popState();
    }

    public void nextValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        if (c == INT_COMMA) {
            listValue(ctx);
        } else if (c == INT_RBRACKET) {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Expecting array next value or end");
        }
    }

    public void beginObject(ParserContext ctx) {
    }


    public boolean handleKey(ParserContext ctx) {
        return false;
    }

    public void valueSeparator(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c != INT_COLON) throw new RuntimeException("Expecting ':' key value separator");
    }

    public void stringValue(ParserContext ctx) {
        ctx.skipToQuote();
        ctx.endToken();
        endStringValue(ctx);
    }

    public void endStringValue(ParserContext ctx) {

    }


    public void startNumberValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getNumberValue());
            ctx.startToken(-1);
            numberValue(ctx);
        } else {
            throw new RuntimeException("Illegal number value");
        }
    }

    public void numberValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == INT_PERIOD) {
            ctx.popState();
            ctx.pushState(getFloatValue());
            floatValue(ctx);
        } else {
            ctx.endToken();
            endNumberValue(ctx);
            ctx.rollback();
            ctx.popState();
        }
    }

    public void startIntegerValue(ParserContext ctx) {
        ctx.popState();
        beginIntegerValue(ctx);
    }

    public void beginIntegerValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.startToken(-1);
            integerValue(ctx);
        } else {
            throw new RuntimeException("Illegal integer value");
        }
    }

    public void integerValue(ParserContext ctx) {
        ctx.skipDigits();
        ctx.endToken();
        endNumberValue(ctx);
        ctx.rollback();
    }

    public void endNumberValue(ParserContext ctx) {

    }

    public void floatValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        ctx.endToken();
        endFloatValue(ctx);
        ctx.rollback();
        ctx.popState();
    }
    public void endFloatValue(ParserContext ctx) {

    }

    public void booleanValue(ParserContext ctx) {
        ctx.skipAlphabetic();
        ctx.endToken();
        endBooleanValue(ctx);
        ctx.rollback();
    }

    public void endBooleanValue(ParserContext ctx) {

    }

    public void startObject(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();
        if (c == INT_LCURLY) {
            beginObject(ctx);
            beginKeys(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }

    }

    public void beginKeys(ParserContext ctx) {
        int c = ctx.skipWhitespace();

        if (c == INT_RCURLY) {
            ctx.rollback();
            return;
        }
        if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
        ctx.pushState(getNextKey());
        ctx.startToken(0);
        key(ctx);
    }

    public void keyStart(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        ctx.popState();

        if (c == INT_RCURLY) {
            ctx.rollback();
            return;
        }
        if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
        ctx.startToken(0);
        key(ctx);
    }

    public void nextKey(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == INT_COMMA) {
            ctx.pushState(getKeyStart());
            keyStart(ctx);
        } else if (c == INT_RCURLY) {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Illegal key value");
        }
    }

    public void key(ParserContext ctx) {
        int c = ctx.skipToQuote();
        ctx.endToken();
        if (!handleKey(ctx)) {
            ctx.pushState(getValue());
        }
        valueSeparator(ctx);
    }

}
