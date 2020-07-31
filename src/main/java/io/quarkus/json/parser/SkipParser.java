package io.quarkus.json.parser;

import static io.quarkus.json.parser.IntChar.*;

public class SkipParser implements JsonParser {
    public static final SkipParser PARSER = new SkipParser();

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext();
        ctx.pushState(getStart());
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

    public void startObject(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (c == INT_LCURLY) {
            beginObject(ctx);
            ctx.pushState(getNextKey());
            ctx.pushState(getKeyStart());
            keyStart(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }

    }


    public void startList(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (c == INT_LBRACKET) {
            beginObject(ctx);
            ctx.pushState(getNextValue());
            listValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }

    }


    public void startStringValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            ctx.pushState(getStringValue());
            stringValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void startBooleanValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (c== 't' || c == 'f') {
            ctx.pushState(getBooleanValue());
            ctx.startToken(-1);
            booleanValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void value(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (c == INT_QUOTE) {
            ctx.pushState(getStringValue());
            ctx.startToken(0);
            stringValue(ctx);
        } else if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getNumberValue());
            ctx.startToken(-1);
            numberValue(ctx);
        } else if (c == INT_t || c == INT_f) {
            ctx.pushState(getBooleanValue());
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
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
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
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }

    public void beginObject(ParserContext ctx) {
    }

    public void keyStart(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();

        if (c == INT_RCURLY) {
            ctx.rollback();
            return;
        }
        if (c != INT_QUOTE) throw new RuntimeException("Expected '\"' at character " + ctx.charCount());
        ctx.startToken(0);
        ctx.pushState(getKey());
        key(ctx);
    }

    public void key(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) return;
        ctx.popState();
        ctx.endToken();
        if (!handleKey(ctx)) {
            ctx.pushState(getValue());
        }
        ctx.pushState(getValueSeparator());
        valueSeparator(ctx);
    }

    public boolean handleKey(ParserContext ctx) {
        return false;
    }

    public void valueSeparator(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        if (c != INT_COLON) throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.popState();
    }

    public void stringValue(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) return;
        ctx.endToken();
        endStringValue(ctx);
        ctx.popState();
    }

    public void endStringValue(ParserContext ctx) {

    }


    public void startNumberValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getNumberValue());
            ctx.startToken(-1);
            numberValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void numberValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) return;
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
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        ctx.popState();
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.pushState(getIntegerValue());
            ctx.startToken(-1);
            integerValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void integerValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) return;
        ctx.endToken();
        endNumberValue(ctx);
        ctx.rollback();
        ctx.popState();
    }

    public void endNumberValue(ParserContext ctx) {

    }

    public void floatValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) return;
        ctx.endToken();
        endFloatValue(ctx);
        ctx.rollback();
        ctx.popState();
    }
    public void endFloatValue(ParserContext ctx) {

    }

    public void booleanValue(ParserContext ctx) {
        int c = ctx.skipAlphabetic();
        if (c == 0) return;
        ctx.popState();
        ctx.endToken();
        endBooleanValue(ctx);
        ctx.rollback();
    }

    public void endBooleanValue(ParserContext ctx) {

    }

    public void nextKey(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) return;
        if (c == INT_COMMA) {
            ctx.pushState(getKeyStart());
        } else if (c == INT_RCURLY) {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }
}
