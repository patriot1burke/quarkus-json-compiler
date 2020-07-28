package io.quarkus.json.parser;

import java.util.HashMap;

public class PersonParser extends SkipParser {

    public static final PersonParser PARSER = new PersonParser();

    public void start(ParserContext ctx) {
        startObject(ctx);
    }

    @Override
    public void appendToken(ParserContext ctx, char c) {
        ctx.token().append(c);
    }


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
            ctx.pushState(this::intMapStart);
        }
        else if (key.equals("dad")) {
            ctx.pushState(this::dadStart);
        }
        else {
            ctx.pushState(SkipParser.PARSER::value);
        }
        return true;
    }

    public void dadStart(ParserContext ctx) {
        ctx.popState();
        ctx.pushState(this::dadEnd);
        ctx.pushState(PersonParser.PARSER::start);
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
