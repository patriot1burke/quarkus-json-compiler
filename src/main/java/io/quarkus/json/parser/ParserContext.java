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

    protected int tokenStart = -1;
    protected int tokenEnd = -1;

    public int consume() {
        if (ptr + 1 > buffer.length) return 0;
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

    public void startToken(int offset) {
        tokenStart = ptr + offset;
    }

    public void endToken() {
        tokenEnd = ptr - 1;
    }

    public String popToken() {
        if (tokenStart < 0) throw new RuntimeException("Token not started.");
        if (tokenEnd < 0) throw new RuntimeException("Token not ended.");
        char[] charbuf = new char[tokenEnd - tokenStart];
        for (int i = 0; i < tokenEnd - tokenStart; i++) charbuf[i] = (char)buffer[tokenStart + i];
        return new String(charbuf);
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
        byte[] bytes = new byte[0];
        try {
            bytes = fullJson.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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
