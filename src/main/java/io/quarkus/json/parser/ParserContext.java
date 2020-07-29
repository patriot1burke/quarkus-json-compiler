package io.quarkus.json.parser;

import java.util.Stack;

public class ParserContext {
    protected int charCount = 0;
    protected Stack<ParserState> state = new Stack<>();
    protected Stack<Object> target = new Stack<>();
    protected StringBuilder token;
    protected char curr;
    protected boolean consumed;

    public char consume() {
        consumed = true;
        return curr;
    }

    public void push(char c) {
        consumed = false;
        curr = c;
    }

    public StringBuilder token() {
        if (token == null) token = new StringBuilder();
        return token;
    }

    public String popToken() {
        StringBuilder tmp = token;
        token = null;
        return tmp.toString();
    }

    public void parse(char c) {
        charCount++;
        consumed = false;
        curr = c;
        if (state.empty()) {
            if (Character.isWhitespace(c)) {
                return;
            } else {
                throw new RuntimeException("Illegal character at " + charCount);
            }
        }
        while (!consumed) {
            state.peek().parse(this);
        }
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
