package io.quarkus.json.parser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayDeque;
import static io.quarkus.json.parser.IntChar.*;

public class ParserContext {
    protected int charCount = 0;
    protected ArrayDeque<ParserState> state = new ArrayDeque<>();
    protected ArrayDeque<Object> target = new ArrayDeque<>();
    protected byte[] buffer;
    protected int ptr;
    protected ParserState initialState;


    protected int tokenStart = -1;
    protected int tokenEnd = -1;

    public ParserContext(ParserState initialState) {
        this.initialState = initialState;
        state.push(initialState);
    }

    public void reset() {
        charCount = 0;
        state.clear();
        target.clear();
        buffer = null;
        ptr = 0;
        tokenStart = -1;
        tokenEnd = -1;
        state.push(initialState);
    }

    public boolean isBufferEmpty() {
        return ptr >= buffer.length;
    }

    public int consume() {
        if (ptr >= buffer.length) return 0;
        return (int) buffer[ptr++] & 0xFF;
    }

    public void rollback() {
        ptr--;
        if (ptr < 0) throw new RuntimeException("Busted buffer bounds");
    }

    public int skipWhitespace() {
        while (ptr < buffer.length) {
            int ch = buffer[ptr++] & 0xFF;
            if (isWhitespace(ch)) continue;
            return ch;
        }
        return 0;
    }

    public int skipToQuote() {
        while (ptr < buffer.length) {
            int ch = buffer[ptr++] & 0xFF;
            if (ch != INT_QUOTE) continue;
            return ch;
        }
        return 0;
    }
    public int skipDigits() {
        while (ptr < buffer.length) {
            int ch = buffer[ptr++] & 0xFF;
            if (isDigit(ch)) continue;
            return ch;
        }
        return 0;
    }

    public int skipAlphabetic() {
        while (ptr < buffer.length) {
            int ch = buffer[ptr++] & 0xFF;
            if (Character.isAlphabetic(ch)) continue;
            return ch;
        }
        return 0;
    }

    public boolean tokenEquals(Symbol symbol) {
        if (tokenEnd - tokenStart != symbol.getUtf8().length) return false;

        for (int i = 0; i < symbol.utf8.length; i++) {
            if (symbol.utf8[i] != buffer[tokenStart + i]) return false;
        }
        return true;
    }

    public void startToken(int offset) {
        tokenStart = ptr + offset;
    }

    public void endToken() {
        tokenEnd = ptr - 1;
    }

    public void clearToken() {
        tokenStart = -1;
        tokenEnd = -1;
    }

    public String popToken() {
        if (tokenStart < 0) throw new RuntimeException("Token not started.");
        if (tokenEnd < 0) throw new RuntimeException("Token not ended.");
        char[] charbuf = new char[tokenEnd - tokenStart];
        for (int i = 0; i < tokenEnd - tokenStart; i++) charbuf[i] = (char)(buffer[tokenStart + i] & 0xFF);
        clearToken();
        return new String(charbuf);
    }

    static int[] TRUE_VALUE = {INT_t, INT_r, INT_u, INT_e};
    static int[] FALSE_VALUE = {INT_f, INT_a, INT_l, INT_s, INT_e};

    public boolean popBooleanToken() {
        if (tokenStart < 0) throw new RuntimeException("Token not started.");
        if (tokenEnd < 0) throw new RuntimeException("Token not ended.");
        int len = tokenEnd - tokenStart;
        if (len == 4) {
            for (int i = 0; i < 4; i++) {
                if (TRUE_VALUE[i] != (int)(buffer[tokenStart + i] & 0xFF)) {
                    break;
                }
            }
            return true;
        } else if (len == 5) {
            for (int i = 0; i < 5; i++) {
                if (FALSE_VALUE[i] != (int)(buffer[tokenStart + i] & 0xFF)) {
                    break;
                }
            }
            return false;

        }
        throw new RuntimeException("Illegal boolean true value syntax");
    }

    public int popIntToken() {
        return (int) popLongToken();
    }

    public long popLongToken() {
        boolean negative = false;
        int i = 0;
        int len = tokenEnd - tokenStart;
        long limit = -9223372036854775807L;
        if (len <= 0) {
            return 0;
        } else {
            int firstChar = buffer[tokenStart] & 0xFF;
            if (firstChar < INT_0) {
                if (firstChar == INT_MINUS) {
                    negative = true;
                    limit = -9223372036854775808L;
                } else if (firstChar != INT_PLUS) {
                    throw new RuntimeException("Illegal number format at character " + charCount);
                }

                if (len == 1) {
                    throw new RuntimeException("Illegal number format at character " + charCount);
                }

                ++i;
            }

            long multmin = limit / (long)10;

            long result;
            int digit;
            for(result = 0L; i < len; result -= (long)digit) {
                digit = (buffer[i++ + tokenStart] & 0xFF) - INT_0;
                if (digit < 0 || result < multmin) {
                    throw new RuntimeException("Illegal number format at character " + charCount);
                }

                result *= (long)10;
                if (result < limit + (long)digit) {
                    throw new RuntimeException("Illegal number format at character " + charCount);
                }
            }

            clearToken();
            return negative ? result : -result;
        }
    }

    public void read(byte[] buffer) {
        charCount++;
        this.buffer = buffer;
        this.ptr = 0;

        while (ptr < this.buffer.length) {
            if (state.isEmpty()) return;
            state.peek().parse(this);
        }
    }

    public <T> T parse(String fullJson) {
        byte[] bytes = null;
        try {
            bytes = fullJson.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        return parse(bytes);
    }

    public <T> T parse(byte[] bytes) {
        read(bytes);
        return target();
    }

    public int charCount() {
        return charCount;
    }

    public void pushState(ParserState state) {
        this.state.push(state);
    }

    public void popState() {
        this.state.pop();
    }

    public <T> T target() {
        return (T)target.peek();
    }

    public void pushTarget(Object obj) {

        target.push(obj);
    }

    public <T> T popTarget() {
        return (T)target.pop();
    }
}
