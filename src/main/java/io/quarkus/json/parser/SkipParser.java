package io.quarkus.json.parser;

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
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '{') {
            beginObject(ctx);
            ctx.pushState(getNextKey());
            ctx.pushState(getKeyStart());
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }

    }


    public void startList(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '[') {
            beginObject(ctx);
            ctx.pushState(getNextValue());
            listValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }

    }


    public void startStringValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '"') {
            ctx.pushState(getStringValue());
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void startBooleanValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == 't' || c == 'f') {
            ctx.pushState(getBooleanValue());
            appendToken(ctx, c);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void value(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '"') {
            ctx.pushState(getStringValue());
        } else if (Character.isDigit(c)) {
            ctx.pushState(getNumberValue());
            appendToken(ctx, c);
        } else if (c == 't' || c == 'f') {
            ctx.pushState(getBooleanValue());
            appendToken(ctx, c);
        } else if (c == '{') {
            beginObject(ctx);
            ctx.pushState(getNextKey());
            ctx.pushState(getKeyStart());
        } else if (c == '[') {
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
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c == ',') {
            listValue(ctx);
        } else if (c == ']') {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }

    public void beginObject(ParserContext ctx) {
    }

    public void keyStart(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();

        if (c == '}') {
            ctx.push(c);
            return;
        }
        if (c != '"') throw new RuntimeException("Expected '\"' at character " + ctx.charCount());
        ctx.pushState(getKey());
    }

    public void key(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            appendToken(ctx, c);
            return;
        }
        ctx.popState();
        if (!handleKey(ctx)) {
            ctx.pushState(getValue());
        }
        ctx.pushState(getValueSeparator());
    }

    public boolean handleKey(ParserContext ctx) {
        return false;
    }

    public void valueSeparator(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c != ':') throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.popState();
    }

    public void appendToken(ParserContext ctx, char c) {

    }

    public void stringValue(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            appendToken(ctx, c);
        } else {
            endStringValue(ctx);
            ctx.popState();
        }
    }

    public void endStringValue(ParserContext ctx) {

    }


    public void startNumberValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (Character.isDigit(c)) {
            ctx.pushState(getNumberValue());
            appendToken(ctx, c);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void numberValue(ParserContext ctx) {
        char c = ctx.consume();
        if (c == '.') {
            ctx.popState();
            ctx.pushState(getFloatValue());
            appendToken(ctx, c);
        } else if (Character.isDigit(c)) {
            appendToken(ctx, c);
        } else {
            endNumberValue(ctx);
            ctx.push(c);
            ctx.popState();
        }
    }

    public void startIntegerValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (Character.isDigit(c)) {
            ctx.pushState(getIntegerValue());
            appendToken(ctx, c);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void integerValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isDigit(c)) {
            appendToken(ctx, c);
        } else {
            endNumberValue(ctx);
            ctx.push(c);
            ctx.popState();
        }
    }

    public void endNumberValue(ParserContext ctx) {

    }

    public void floatValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isDigit(c)) {
            appendToken(ctx, c);
        } else {
            endFloatValue(ctx);
            ctx.push(c);
            ctx.popState();
        }
    }
    public void endFloatValue(ParserContext ctx) {

    }

    public void booleanValue(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isAlphabetic(c)) {
            appendToken(ctx, c);
        } else {
            endBooleanValue(ctx);
            ctx.push(c);
            ctx.popState();
        }
    }
    public void endBooleanValue(ParserContext ctx) {

    }

    public void nextKey(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c == ',') {
            ctx.pushState(getKeyStart());
        } else if (c == '}') {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }
}
