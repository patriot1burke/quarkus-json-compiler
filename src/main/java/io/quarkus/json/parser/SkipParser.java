package io.quarkus.json.parser;

public class SkipParser {
    public static final SkipParser PARSER = new SkipParser();

    public void start(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '{') {
            startObject(ctx);
            ctx.pushState(this::nextKey);
            ctx.pushState(this::keyStart);
        } else if (c == '[') {
            startList(ctx);
            ctx.pushState(this::nextValue);
            listValue(ctx);
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount);
        }
    }

    public void startList(ParserContext ctx) {

    }

    public void listValue(ParserContext ctx) {
        ctx.pushState(this::addListValue);
        ctx.pushState(this::valueStart);
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

    public void startObject(ParserContext ctx) {
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
        ctx.pushState(this::key);
    }

    public void key(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            appendToken(ctx, c);
            return;
        }
        ctx.popState();
        handleKey(ctx);
        ctx.pushState(this::valueSeparator);
    }

    public void handleKey(ParserContext ctx) {

    }

    public void valueSeparator(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c != ':') throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.popState();
        ctx.pushState(this::valueStart);
    }

    public void appendToken(ParserContext ctx, char c) {

    }

    public void valueStart(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '"') {
            ctx.pushState(this::stringValue);
        } else if (Character.isDigit(c)) {
            ctx.pushState(this::numberValue);
            appendToken(ctx, c);
        } else if (c == 't' || c == 'f') {
            ctx.pushState(this::booleanValue);
            appendToken(ctx, c);
        } else if (c == '{' || c == '[') {
            ctx.push(c);
            ctx.pushState(this::start);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
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


    public void numberValue(ParserContext ctx) {
        char c = ctx.consume();
        if (c == '.') {
            ctx.popState();
            ctx.pushState(this::floatValue);
            appendToken(ctx, c);
        } else if (Character.isDigit(c)) {
            appendToken(ctx, c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
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
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
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
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
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
            ctx.pushState(this::keyStart);
        } else if (c == '}') {
            ctx.popState();
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }
}
