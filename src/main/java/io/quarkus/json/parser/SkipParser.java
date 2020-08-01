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
    private ParserState startStringValue  = this::startStringValue;
    public ParserState getStartStringValue() {
        return startStringValue;
    }
    private ParserState startIntegerValue  = this::startIntegerValue;
    public ParserState getStartIntegerValue() {
        return startIntegerValue;
    }

    public void start(ParserContext ctx) {
        unpushedValue(ctx);
    }

    public void startList(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == INT_LBRACKET) {
            beginObject(ctx);
            loopListValues(ctx);
        } else {
            throw new RuntimeException("Expecting start of array");
        }
    }

    public void startStringValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            stringValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public void startBooleanValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c== 't' || c == 'f') {
            ctx.startToken(-1);
            booleanValue(ctx);
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
            ctx.startToken(-1);
            numberValue(ctx);
        } else if (c == INT_t || c == INT_f) {
            ctx.startToken(-1);
            booleanValue(ctx);
        } else if (c == INT_LCURLY) {
            beginObject(ctx);
            loopKeys(ctx);
        } else if (c == INT_LBRACKET) {
            beginList(ctx);
            loopListValues(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public void loopListValues(ParserContext ctx) {
        boolean first = true;

        while (true) {
            int c = ctx.skipWhitespace();
            if (c == INT_RBRACKET) {
                return;
            }

            if (first) {
                first = false;
                ctx.rollback();
                listValue(ctx);
                addListValue(ctx);
            } else {
                if (c != INT_COMMA) throw new RuntimeException("Expecting comma separator");
                listValue(ctx);
                addListValue(ctx);
            }
        }

    }

    public void listValue(ParserContext ctx) {
        unpushedValue(ctx);
    }



    public void beginList(ParserContext ctx) {

    }

    public void addListValue(ParserContext ctx) {
    }

    public void beginObject(ParserContext ctx) {
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

    public void startIntegerValue(ParserContext ctx) {
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

    public void startNumberValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.startToken(-1);
            numberValue(ctx);
        } else {
            throw new RuntimeException("Illegal number value");
        }
    }

    public void numberValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == INT_PERIOD) {
            floatValue(ctx);
        } else {
            ctx.endToken();
            endNumberValue(ctx);
            ctx.rollback();
        }
    }

    public void floatValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        ctx.endToken();
        endFloatValue(ctx);
        ctx.rollback();
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
        if (c == INT_LCURLY) {
            beginObject(ctx);
            loopKeys(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }

    }

    public void loopKeys(ParserContext ctx) {
        boolean first = true;

        while (true) {
            int c = ctx.skipWhitespace();
            if (c == INT_RCURLY) {
                return;
            }

            if (first) {
                first = false;
                if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
                key(ctx);
            } else {
                if (c != INT_COMMA) throw new RuntimeException("Expecting comma separator");
                c = ctx.skipWhitespace();
                if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
                key(ctx);
            }
        }
    }

    public void key(ParserContext ctx) {
        ctx.startToken(0);
        int c = ctx.skipToQuote();
        ctx.endToken();
        String key = ctx.popToken();
        valueSeparator(ctx);
        unpushedValue(ctx);
    }

}
