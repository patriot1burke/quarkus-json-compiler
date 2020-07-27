package io.quarkus.json.parser;

import java.util.HashMap;
import java.util.HashSet;

public class PersonParser extends ObjectParser implements JsonParser {

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext();
        ctx.pushTarget(new Person());
        ctx.state().push(this::start);
        return ctx;
    }

    @Override
    void handleKey(String key, ParserContext ctx) {
        if (key.equals("name")) {
            ctx.handler(this::nameStart);
            ctx.state().push(this::valueSeparator);
        } else if (key.equals("age")) {
            ctx.handler(this::ageStart);
            ctx.state().push(this::valueSeparator);
        } else if (key.equals("money")) {
            ctx.handler(this::moneyStart);
            ctx.state().push(this::valueSeparator);
        } else if (key.equals("married")) {
            ctx.handler(this::marriedStart);
            ctx.state().push(this::valueSeparator);
        } else if (key.equals("intMap")) {
            ctx.state().push(this::intMapStart);
        } else {
            ctx.handler(this::_skipValue);
            ctx.state().push(this::valueSeparator);
        }
    }

    public void intMapStart(char c, ParserContext ctx) {
        if (Character.isWhitespace(c)) return;
        if (c != ':') throw new RuntimeException("Expecting ':' at character " + ctx.charCount());
        ctx.state().pop();

        Person person = ctx.target();
        person.setIntMap(new HashMap<>());
        MapParser mapParser = new MapParser() {
            @Override
            void setValue(String property, String value, ParserContext ctx) {
                person.getIntMap().put(property, Integer.valueOf(value));
            }
        };
        ctx.state().push(this::nextKey);
        ctx.state().push(mapParser::start);
    }

    public void nameStart(ParserContext ctx) {
        ctx.handler(this::nameEnd);
    }

    public void nameEnd(ParserContext ctx) {
        Person person = ctx.target();
        person.setName(ctx.popToken());
        ctx.state().push(this::nextKey);
    }

    public void ageStart(ParserContext ctx) {
        ctx.handler(this::ageEnd);
    }

    public void ageEnd(ParserContext ctx) {
        Integer value = Integer.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setAge(value);
        ctx.state().push(this::nextKey);
    }

    public void moneyStart(ParserContext ctx) {
        ctx.handler(this::moneyEnd);
    }

    public void moneyEnd(ParserContext ctx) {
        Float value = Float.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setMoney(value);
        ctx.state().push(this::nextKey);
    }

    public void marriedStart(ParserContext ctx) {
        ctx.handler(this::marriedEnd);
    }

    public void marriedEnd(ParserContext ctx) {
        Person person = ctx.target();
        person.setMarried(Boolean.parseBoolean(ctx.popToken()));
        ctx.state().push(this::nextKey);
    }

}
