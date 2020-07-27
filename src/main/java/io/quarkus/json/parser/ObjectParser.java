package io.quarkus.json.parser;

public abstract class ObjectParser {

    public void start(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c != '{') throw new RuntimeException("Expected '{' at character " + ctx.charCount());

        ctx.state().pop();
        ctx.state().push(this::keyStart);
    }

    public void keyStart(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c == '}') {
            ctx.state().pop();
            return;
        }
        if (c != '"') throw new RuntimeException("Expected '\"' at character " + ctx.charCount());
        ctx.state().pop();
        ctx.state().push(this::key);
    }

    abstract void handleKey(String key, ParserContext ctx);

    public void key(char c, ParserContext ctx) {
        if (c != '"') {
            ctx.token().append(c);
            return;
        }
        String key = ctx.popToken();
        ctx.state().pop();
        handleKey(key, ctx);
    }

    public void valueSeparator(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c != ':') throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.state().pop();
        ctx.state().push(this::valueStart);
    }

    public void valueStart(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c == '"') {
            ctx.state().pop();
            ctx.state().push(this::stringValue);
            ctx.handler().handle(ctx);
        } else if (Character.isDigit(c)) {
            ctx.state().pop();
            ctx.state().push(this::numberValue);
            ctx.token().append(c);
            ctx.handler().handle(ctx);
        } else if (c == 't' || c == 'f') {
            ctx.state().pop();
            ctx.state().push(this::booleanValue);
            ctx.token().append(c);
            ctx.handler().handle(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax at character " + ctx.charCount());
        }
    }

    public void stringValue(char c, ParserContext ctx) {
        if (c == '"') {
            ctx.state().pop();
            ctx.handler().handle(ctx);
        } else {
            ctx.token().append(c);
        }
    }

    public void numberValue(char c, ParserContext ctx) {
        if (c == '.') {
            ctx.state().pop();
            ctx.state().push(this::floatValue);
            ctx.token().append(c);
        } else if (Character.isDigit(c)) {
            ctx.token().append(c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
        } else {
            ctx.state().pop();
            ctx.handler().handle(ctx);
            ctx.parse(c);
        }
    }

    public void floatValue(char c, ParserContext ctx) {
        if (Character.isDigit(c)) {
            ctx.token().append(c);
        } else if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            throw new RuntimeException("Illegal character at " + ctx.charCount());
        } else {
            ctx.state().pop();
            ctx.handler().handle(ctx);
            ctx.parse(c);
        }
    }

    public void booleanValue(char c, ParserContext ctx) {
        if (!Character.isWhitespace(c) && c != ',' && c != '}') {
            ctx.token().append(c);
        } else {
            ctx.state().pop();
            ctx.handler().handle(ctx);
            ctx.parse(c);
        }
    }

    public void nextKey(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c == ',') {
            ctx.state().pop();
            ctx.state().push(this::keyStart);
        } else if (c == '}') {
            ctx.state().pop();
            return;
        } else {
            throw new RuntimeException("Illegal character at character " + ctx.charCount());
        }
    }

    public void _skipValue(ParserContext ctx) {
        ctx.state().push(this::valueStart);
        ctx.handler(this::_skipValueEnd);
    }

    public void _skipValueEnd(ParserContext ctx) {
        ctx.popToken();
        ctx.state().push(this::nextKey);
    }
}
