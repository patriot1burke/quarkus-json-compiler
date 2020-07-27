package io.quarkus.json.parser;

public abstract class ObjectParser {

    public void start(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c != '{') throw new RuntimeException("Expected '{' at character " + ctx.charCount());
        ctx.popState();
        ctx.pushState(this::keyStart);
    }

    public void keyStart(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();

        if (c == '}') {
            return;
        }
        if (c != '"') throw new RuntimeException("Expected '\"' at character " + ctx.charCount());
        ctx.pushState(this::key);
    }

    abstract void handleKey(String key, ParserContext ctx);

    public void key(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            ctx.token().append(c);
            return;
        }
        ctx.popState();
        ctx.pushState(this::nextKey);
        String key = ctx.popToken();
        handleKey(key, ctx);
        ctx.pushState(this::valueSeparator);
    }

    public void valueSeparator(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        if (c != ':') throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.popState();
    }

    public void valueStart(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == '"') {
            ctx.pushState(this::stringValue);
        } else if (Character.isDigit(c)) {
            ctx.pushState(this::numberValue);
            ctx.token().append(c);
        } else if (c == 't' || c == 'f') {
            ctx.pushState(this::booleanValue);
            ctx.token().append(c);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void stringValue(ParserContext ctx) {
        char c = ctx.consume();
        if (c != '"') {
            ctx.token().append(c);
        } else {
            ctx.popState();
        }
    }

    public void numberValue(ParserContext ctx) {
        char c = ctx.peek();
        if (c == '.') {
            ctx.consume();
            ctx.popState();
            ctx.pushState(this::floatValue);
            ctx.token().append(c);
        } else if (Character.isDigit(c)) {
            ctx.consume();
            ctx.token().append(c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
        } else {
            ctx.popState();
        }
    }

    public void floatValue(ParserContext ctx) {
        char c = ctx.peek();
        if (Character.isDigit(c)) {
            ctx.consume();
            ctx.token().append(c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
        } else {
            ctx.popState();
        }
    }

    public void booleanValue(ParserContext ctx) {
        char c = ctx.peek();
        if (Character.isAlphabetic(c)) {
            ctx.consume();
            ctx.token().append(c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
        } else {
            ctx.popState();
        }
    }

    public void nextKey(ParserContext ctx) {
        char c = ctx.consume();
        if (Character.isWhitespace(c)) return;
        ctx.popState();
        if (c == ',') {
            ctx.pushState(this::keyStart);
        } else if (c == '}') {
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }
}
