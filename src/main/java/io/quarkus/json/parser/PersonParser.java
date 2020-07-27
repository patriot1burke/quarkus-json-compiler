package io.quarkus.json.parser;

import java.util.HashMap;

public class PersonParser extends ObjectParser implements JsonParser {

    @Override
    public ParserContext parser() {
        ParserContext ctx = new ParserContext();
        ctx.pushTarget(new Person());
        ctx.pushState(this::start);
        return ctx;
    }

    @Override
    void handleKey(String key, ParserContext ctx) {
        if (key.equals("name")) {
            ctx.pushState(this::nameEnd);
            ctx.pushState(this::valueStart);
        } else if (key.equals("age")) {
            ctx.pushState(this::ageEnd);
            ctx.pushState(this::valueStart);
        } else if (key.equals("money")) {
            ctx.pushState(this::moneyEnd);
            ctx.pushState(this::valueStart);
        } else if (key.equals("married")) {
            ctx.pushState(this::marriedEnd);
            ctx.pushState(this::valueStart);
        } else if (key.equals("intMap")) {
            ctx.pushState(this::intMapStart);
        } else if (key.equals("dad")) {
            ctx.pushState(this::dadStart);
        } else {
            throw new RuntimeException("Unknown key");
        }
    }

    public void dadStart(ParserContext ctx) {
        ctx.popState();
        PersonParser dadParser = new PersonParser();
        ctx.pushTarget(new Person());
        ctx.pushState(this::dadEnd);
        ctx.pushState(dadParser::start);
    }

    public void dadEnd(ParserContext ctx) {
        ctx.popState();
        Person dad = ctx.popTarget();
        Person person = ctx.target();
        person.setDad(dad);
    }

    public void intMapStart(ParserContext ctx) {
        ctx.popState();
        Person person = ctx.target();
        person.setIntMap(new HashMap<>());
        MapParser mapParser = new MapParser() {
            @Override
            void setValue(String property, String value, ParserContext ctx) {
                person.getIntMap().put(property, Integer.valueOf(value));
            }
        };
        ctx.pushState(mapParser::start);
    }

    public void nameEnd(ParserContext ctx) {
        ctx.popState();
        Person person = ctx.target();
        person.setName(ctx.popToken());
    }
    
    public void ageEnd(ParserContext ctx) {
        ctx.popState();
        Integer value = Integer.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setAge(value);
    }


    public void moneyEnd(ParserContext ctx) {
        ctx.popState();
        Float value = Float.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setMoney(value);
    }
    
    public void marriedEnd(ParserContext ctx) {
        ctx.popState();
        Person person = ctx.target();
        person.setMarried(Boolean.parseBoolean(ctx.popToken()));
    }

}
