package io.quarkus.json.parser.test;

import io.quarkus.json.parser.CollectionParser;
import io.quarkus.json.parser.ContextValue;
import io.quarkus.json.parser.MapParser;
import io.quarkus.json.parser.ObjectParser;
import io.quarkus.json.parser.ParserContext;
import io.quarkus.json.parser.ParserState;
import io.quarkus.json.parser.SkipParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ExamplePersonParser extends ObjectParser {

    public static final ExamplePersonParser PARSER = new ExamplePersonParser();

    private ParserState intMapStart = new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
            ObjectParser.PARSER.getStartIntegerValue()).getStart();
    private ParserState kidsStart = new MapParser(ContextValue.STRING_VALUE,
            ContextValue.OBJECT_VALUE,
            getStart()).getStart();
    private ParserState siblingsStart = new CollectionParser(ContextValue.OBJECT_VALUE,
            getStart()).getStart();
    private ParserState petsStart = new CollectionParser(ContextValue.STRING_VALUE,
            ObjectParser.PARSER.getStartStringValue()).getStart();

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new Person());
    }

    @Override
    public boolean handleKey(ParserContext ctx) {
        String key = ctx.popToken();
        if (key.equals("age")) {
            ageBegin(ctx);
        } else if (key.equals("dad")) {
            dadBegin(ctx);
        } else if (key.equals("intMap")) {
            intMapBegin(ctx);
        } else if (key.equals("kids")) {
            kidsBegin(ctx);
        } else if (key.equals("married")) {
            marriedBegin(ctx);
        } else if (key.equals("money")) {
            moneyBegin(ctx);
        } else if (key.equals("name")) {
            nameBegin(ctx);
        } else if (key.equals("pets")) {
            petsBegin(ctx);
        } else if (key.equals("siblings")) {
            siblingsBegin(ctx);
        }
        else {
            ctx.pushState(SkipParser.PARSER.getValue());
        }
        return true;
    }

    public void siblingsBegin(ParserContext ctx) {
        ctx.pushTarget(new LinkedList<>());
        ctx.pushState(siblingsEnd);
        ctx.pushState(siblingsStart);
    }

    public void petsBegin(ParserContext ctx) {
        ctx.pushTarget(new LinkedList());
        ctx.pushState(petsEnd);
        ctx.pushState(petsStart);
    }

    public void nameBegin(ParserContext ctx) {
        ctx.pushState(nameEnd);
        ctx.pushState(getStartStringValue());
    }

    public void moneyBegin(ParserContext ctx) {
        ctx.pushState(moneyEnd);
        ctx.pushState(getStartNumberValue());
    }

    public void marriedBegin(ParserContext ctx) {
        ctx.pushState(marriedEnd);
        ctx.pushState(getStartBooleanValue());
    }

    public void kidsBegin(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
        ctx.pushState(kidsEnd);
        ctx.pushState(kidsStart);
    }

    public void intMapBegin(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
        ctx.pushState(intMapEnd);
        ctx.pushState(intMapStart);
    }

    public void dadBegin(ParserContext ctx) {
        ctx.pushState(dadEnd);
        ctx.pushState(ExamplePersonParser.PARSER.getStart());
    }

    public void ageBegin(ParserContext ctx) {
        ctx.pushState(ageEnd);
        ctx.pushState(getStartNumberValue());
    }

    static ParserState siblingsEnd = (ctx) -> {
        ctx.popState();
        List<Person> siblings = ctx.popTarget();
        Person person = ctx.target();
        person.setSiblings(siblings);
    };

    static ParserState petsEnd = (ctx) -> {
        ctx.popState();
        List<String> pets = ctx.popTarget();
        Person person = ctx.target();
        person.setPets(pets);
    };

    static ParserState kidsEnd = (ctx) -> {
        ctx.popState();
        Map<String, Person> kids = ctx.popTarget();
        Person person = ctx.target();
        person.setKids(kids);
    };

    static ParserState dadEnd = (ctx) -> {
        ctx.popState();
        Person dad = ctx.popTarget();
        Person person = ctx.target();
        person.setDad(dad);
    };

    static ParserState intMapEnd = (ctx) -> {
        ctx.popState();
        Map<String, Integer> intMap = ctx.popTarget();
        Person person = ctx.target();
        person.setIntMap(intMap);
    };

    static ParserState nameEnd = (ctx) -> {
        ctx.popState();
        Person person = ctx.target();
        person.setName(ctx.popToken());
    };

    static ParserState ageEnd = (ctx) -> {
        ctx.popState();
        int value = Integer.parseInt(ctx.popToken());
        Person person = ctx.target();
        person.setAge(value);
    };

    static ParserState moneyEnd = (ctx) -> {
        ctx.popState();
        float value = Float.parseFloat(ctx.popToken());
        Person person = ctx.target();
        person.setMoney(value);
    };

    static ParserState marriedEnd = (ctx) -> {
        ctx.popState();
        Person person = ctx.target();
        person.setMarried(Boolean.parseBoolean(ctx.popToken()));
    };
}
