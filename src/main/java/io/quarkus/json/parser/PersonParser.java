package io.quarkus.json.parser;

import java.util.Map;

public class PersonParser extends ObjectParser {

    public static final PersonParser PARSER = new PersonParser();

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new Person());
    }

    @Override
    public boolean handleKey(ParserContext ctx) {
        String key = ctx.popToken();
        if (key.equals("name")) {
            ctx.pushState(this::nameEnd);
            ctx.pushState(this::value);
        } else if (key.equals("age")) {
            ctx.pushState(this::ageEnd);
            ctx.pushState(this::value);
        } else if (key.equals("money")) {
            ctx.pushState(this::moneyEnd);
            ctx.pushState(this::value);
        } else if (key.equals("married")) {
            ctx.pushState(this::marriedEnd);
            ctx.pushState(this::value);
        } else if (key.equals("intMap")) {
            ctx.pushState(this::intMapEnd);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE)::start);
        }
        else if (key.equals("dad")) {
            ctx.pushState(this::dadEnd);
            ctx.pushState(PersonParser.PARSER::start);
        }
        else {
            ctx.pushState(SkipParser.PARSER::value);
        }
        return true;
    }

    public void dadEnd(ParserContext ctx) {
        ctx.popState();
        Person dad = ctx.popTarget();
        Person person = ctx.target();
        person.setDad(dad);
    }

    public void intMapEnd(ParserContext ctx) {
        ctx.popState();
        Map<String, Integer> intMap = ctx.popTarget();
        Person person = ctx.target();
        person.setIntMap(intMap);
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
