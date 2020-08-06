package io.quarkus.json.nio;

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
    // they are referenced
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
    private ParserState continueStartStringValue = this::continueStartStringValue;
    private ParserState getContinueStartStringValue() {
        return continueStartStringValue;
    }
    private ParserState continueStartBooleanValue = this::continueStartBooleanValue;
    public ParserState getContinueStartBooleanValue() {
        return continueStartBooleanValue;
    }
    private ParserState continueValue= this::continueValue;
    public ParserState getContinueValue() {
        return continueValue;
    }
    private ParserState continueLoopListValues = this::continueLoopListValues;
    public ParserState getContinueLoopListValues() {
        return continueLoopListValues;
    }
    private ParserState continueNextListValues = this::continueNextListValues;
    public ParserState getContinueNextListValues() {
        return continueNextListValues;
    }
    private ParserState continueValueSeparator = this::continueValueSeparator;
    public ParserState getContinueValueSeparator() {
        return continueValueSeparator;
    }
    private ParserState continueStringValue = this::continueStringValue;
    public ParserState getContinueStringValue() {
        return continueStringValue;
    }
    private ParserState continueStartIntegerValue = this::continueStartIntegerValue;
    public ParserState getContinueStartIntegerValue() {
        return continueStartIntegerValue;
    }
    private ParserState continueIntegerValue = this::continueIntegerValue;
    public ParserState getContinueIntegerValue() {
        return continueIntegerValue;
    }
    private ParserState continueStartNumberValue = this::continueStartNumberValue;
    public ParserState getContinueStartNumberValue() {
        return continueStartNumberValue;
    }
    private ParserState continueNumberValue = this::continueNumberValue;
    public ParserState getContinueNumberValue() {
        return continueNumberValue;
    }
    private ParserState continueFloatValue = this::continueFloatValue;
    public ParserState getContinueFloatValue() {
        return continueFloatValue;
    }
    private ParserState continueBooleanValue = this::continueBooleanValue;
    public ParserState getContinueBooleanValue() {
        return continueBooleanValue;
    }
    private ParserState continueStartObject = this::continueStartObject;
    public ParserState getContinueStartObject() {
        return continueStartObject;
    }
    private ParserState continueLoopKeys = this::continueLoopKeys;
    public ParserState getContinueLoopKeys() {
        return continueLoopKeys;
    }
    private ParserState continueNextKeys = this::continueNextKeys;
    public ParserState getContinueNextKeys() {
        return continueNextKeys;
    }
    private ParserState continueKey = this::continueKey;
    public ParserState getContinueKey() {
        return continueKey;
    }



    public boolean start(ParserContext ctx) {
        return value(ctx);
    }

    public boolean startList(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == INT_LBRACKET) {
            beginObject(ctx);
            return loopListValues(ctx);
        } else {
            throw new RuntimeException("Expecting start of array");
        }
    }

    public boolean continueStartStringValue(ParserContext ctx) {
        ctx.popState();
        return startStringValue(ctx);
    }

    public boolean startStringValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueStartStringValue());
            return false;
        }
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            return stringValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public boolean continueStartBooleanValue(ParserContext ctx) {
        ctx.popState();
        return startBooleanValue(ctx);
    }

    public boolean startBooleanValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueStartBooleanValue());
            return false;
        }
        if (c== 't' || c == 'f') {
            ctx.startToken(-1);
            return booleanValue(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public boolean continueValue(ParserContext ctx) {
        ctx.popState();
        return value(ctx);
    }

    public boolean value(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueValue());
            return false;
        }
        if (c == INT_QUOTE) {
            ctx.startToken(0);
            return stringValue(ctx);
        } else if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.startToken(-1);
            return numberValue(ctx);
        } else if (c == INT_t || c == INT_f) {
            ctx.startToken(-1);
            return booleanValue(ctx);
        } else if (c == INT_LCURLY) {
            beginObject(ctx);
            return loopKeys(ctx);
        } else if (c == INT_LBRACKET) {
            beginList(ctx);
            return loopListValues(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }

    public boolean continueLoopListValues(ParserContext ctx) {
        ctx.popState();
        return loopListValues(ctx);
    }

    public boolean loopListValues(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueLoopListValues());
            return false;
        }
        if (c == INT_RBRACKET) {
            return true;
        }
        ctx.rewind();
        int stateIndex = ctx.stateIndex();
        if (!listValue(ctx)) {
            ctx.pushState(getContinueNextListValues(), stateIndex);
            return false;
        }
        addListValue(ctx);
        return nextListValues(ctx);
    }

    public boolean continueNextListValues(ParserContext ctx) {
        ctx.popState();
        return nextListValues(ctx);
    }

    public boolean nextListValues(ParserContext ctx) {
        while (true) {
            int c = ctx.skipWhitespace();
            if (c == 0) {
                ctx.pushState(getContinueNextListValues());
                return false;
            }
            if (c == INT_RBRACKET) {
                return true;
            }
            if (c != INT_COMMA) throw new RuntimeException("Expecting comma separator");
            int stateIndex = ctx.stateIndex();
            if (!listValue(ctx)) {
                ctx.pushState(getContinueNextListValues(), stateIndex);
                return false;
            }
            addListValue(ctx);
        }
    }

    public boolean listValue(ParserContext ctx) {
        return value(ctx);
    }

    public void beginList(ParserContext ctx) {

    }

    public void addListValue(ParserContext ctx) {
    }

    public void beginObject(ParserContext ctx) {
    }

    public boolean continueValueSeparator(ParserContext ctx) {
        ctx.popState();
        return valueSeparator(ctx);
    }


    public boolean valueSeparator(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueValueSeparator());
            return false;
        }
        if (c != INT_COLON) throw new RuntimeException("Expecting ':' key value separator");
        return true;
    }

    public boolean continueStringValue(ParserContext ctx) {
        ctx.popState();
        return stringValue(ctx);
    }

    public boolean stringValue(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) {
            ctx.pushState(getContinueStringValue());
            return false;
        }
        ctx.endToken();
        endStringValue(ctx);
        return true;
    }

    public void endStringValue(ParserContext ctx) {

    }

    public boolean continueStartIntegerValue(ParserContext ctx) {
        ctx.popState();
        return startIntegerValue(ctx);
    }

    public boolean startIntegerValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueStartIntegerValue());
            return false;
        }
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.startToken(-1);
            return integerValue(ctx);
        } else {
            throw new RuntimeException("Illegal integer value");
        }
    }

    public boolean continueIntegerValue(ParserContext ctx) {
        ctx.popState();
        return integerValue(ctx);
    }

    public boolean integerValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) {
            ctx.pushState(getContinueIntegerValue());
            return false;
        }
        ctx.endToken();
        endNumberValue(ctx);
        ctx.rewind();
        return true;
    }

    public void endNumberValue(ParserContext ctx) {

    }

    public boolean continueStartNumberValue(ParserContext ctx) {
        ctx.popState();
        return startNumberValue(ctx);
    }
    public boolean startNumberValue(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueStartNumberValue());
            return false;
        }
        if (isDigit(c) || c == INT_MINUS || c == INT_PLUS) {
            ctx.startToken(-1);
            return numberValue(ctx);
        } else {
            throw new RuntimeException("Illegal number value");
        }
    }

    public boolean continueNumberValue(ParserContext ctx) {
        ctx.popState();
        return numberValue(ctx);
    }

    public boolean numberValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) {
            ctx.pushState(getContinueNumberValue());
            return false;
        }
        if (c == INT_PERIOD) {
            return floatValue(ctx);
        } else {
            ctx.endToken();
            endNumberValue(ctx);
            ctx.rewind();
            return true;
        }
    }

    public boolean continueFloatValue(ParserContext ctx) {
        ctx.popState();
        return floatValue(ctx);
    }

    public boolean floatValue(ParserContext ctx) {
        int c = ctx.skipDigits();
        if (c == 0) {
            ctx.pushState(getContinueFloatValue());
        }
        ctx.endToken();
        endFloatValue(ctx);
        ctx.rewind();
        return true;
    }

    public void endFloatValue(ParserContext ctx) {

    }

    public boolean continueBooleanValue(ParserContext ctx) {
        ctx.popState();
        return booleanValue(ctx);
    }

    public boolean booleanValue(ParserContext ctx) {
        int c = ctx.skipAlphabetic();
        if (c == 0) {
            ctx.pushState(getContinueBooleanValue());
            return false;
        }
        ctx.endToken();
        endBooleanValue(ctx);
        ctx.rewind();
        return true;
    }

    public void endBooleanValue(ParserContext ctx) {

    }

    public boolean startObject(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueStartObject());
            return false;
        }
        return handleObject(ctx, c);
    }

    public boolean continueStartObject(ParserContext ctx) {
        ctx.popState();
        return startObject(ctx);
    }

    public boolean handleObject(ParserContext ctx, int c) {
        if (c == INT_LCURLY) {
            beginObject(ctx);
            return loopKeys(ctx);
        } else {
            throw new RuntimeException("Illegal value syntax");
        }
    }
    public boolean continueLoopKeys(ParserContext ctx) {
        ctx.popState();
        return loopKeys(ctx);
    }

    public boolean loopKeys(ParserContext ctx) {
        int c = ctx.skipWhitespace();
        if (c == 0) {
            ctx.pushState(getContinueLoopKeys());
            return false;
        }
        if (c == INT_RCURLY) {
            return true;
        }
        if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
        int stateIndex = ctx.stateIndex();
        if (!key(ctx)) {
            ctx.pushState(getContinueNextKeys(), stateIndex);
            return false;
        }
        return nextKeys(ctx);
    }

    public boolean continueNextKeys(ParserContext ctx) {
        ctx.popState();
        return nextKeys(ctx);
    }

    public boolean nextKeys(ParserContext ctx) {
        do {
            int c = ctx.skipWhitespace();
            if (c == 0) {
                ctx.pushState(getContinueNextKeys());
                return false;
            }
            if (c == INT_RCURLY) {
                return true;
            }

            if (c != INT_COMMA) throw new RuntimeException("Expecting comma separator");
            c = ctx.skipWhitespace();
            if (c == 0) {
                ctx.pushState(getContinueLoopKeys());
                return false;
            }
            if (c != INT_QUOTE) throw new RuntimeException("Expecting key quote");
            int stateIndex = ctx.stateIndex();
            if (!key(ctx)) {
                ctx.pushState(getContinueNextKeys(), stateIndex);
                return false;
            }
        } while (true);
    }

    public boolean key(ParserContext ctx) {
        ctx.startToken(0);
        return handleKey(ctx);
    }

    public boolean handleKey(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) {
            ctx.pushState(getContinueKey());
            return false;
        }
        ctx.clearToken();
        int stateIndex = ctx.stateIndex();
        if (!valueSeparator(ctx)) {
           ctx.pushState(getContinueValue(), stateIndex);
           return false;
        }
        return value(ctx);
    }

    public boolean continueKey(ParserContext ctx) {
        ctx.popState();
        return handleKey(ctx);
    }

}
