package io.quarkus.json.parser;

import java.util.Stack;

public class ParserContext {
    protected int charCount = 0;
    protected Stack<ParserState> state = new Stack<>();
    protected Stack<Object> target = new Stack<>();
    protected StringBuffer token;
    protected ParserHandler handler;

    public StringBuffer token() {
        if (token == null) token = new StringBuffer();
        return token;
    }

    public String popToken() {
        StringBuffer tmp = token;
        token = null;
        return tmp.toString();
    }

    public ParserHandler handler() {
        return handler;
    }

    public void handler(ParserHandler handler) {
        this.handler = handler;
    }

    public void parse(char c) {
        charCount++;
        if (state.empty() && !Character.isWhitespace(c)) {
            throw new RuntimeException("Illegal character at " + charCount);
        }
        state.peek().parse(c, this);
    }

    public int charCount() {
        return charCount;
    }

    public Stack<ParserState> state() {
        return state;
    }

    public <T> T target() {
        return (T)target.peek();
    }

    public void pushTarget(Object obj) {
        target.push(obj);
    }

    public <T> T popTarget(Object obj) {
        return (T)target.pop();
    }
}
