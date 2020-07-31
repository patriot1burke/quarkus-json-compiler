package io.quarkus.json.parser.test;

import io.quarkus.json.parser.CollectionParser;
import io.quarkus.json.parser.ContextValue;
import io.quarkus.json.parser.MapParser;
import io.quarkus.json.parser.ObjectParser;
import io.quarkus.json.parser.ParserContext;
import io.quarkus.json.parser.ParserState;
import io.quarkus.json.parser.SkipParser;
import io.quarkus.json.parser.Symbol;

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
        if (ctx.tokenEquals(ageSymbol)) {
            ctx.pushState(ageEnd);
            ctx.pushState(getStartNumberValue());
        } else if (ctx.tokenEquals(dadSymbol)) {
            ctx.pushState(dadEnd);
            ctx.pushState(ExamplePersonParser.PARSER.getStart());
        } else if (ctx.tokenEquals(intMapSymbol)) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(intMapEnd);
            ctx.pushState(intMapStart);
        } else if (ctx.tokenEquals(kidsSymbol)) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(kidsEnd);
            ctx.pushState(kidsStart);
        } else if (ctx.tokenEquals(marriedSymbol)) {
            ctx.pushState(marriedEnd);
            ctx.pushState(getStartBooleanValue());
        } else if (ctx.tokenEquals(moneySymbol)) {
            ctx.pushState(moneyEnd);
            ctx.pushState(getStartNumberValue());
        } else if (ctx.tokenEquals(nameSymbol)) {
            ctx.pushState(nameEnd);
            ctx.pushState(getStartStringValue());
        } else if (ctx.tokenEquals(petsSymbol)) {
            ctx.pushTarget(new LinkedList());
            ctx.pushState(petsEnd);
            ctx.pushState(petsStart);
        } else if (ctx.tokenEquals(siblingsSymbol)) {
            ctx.pushTarget(new LinkedList<>());
            ctx.pushState(siblingsEnd);
            ctx.pushState(siblingsStart);
        } else {
            ctx.pushState(SkipParser.PARSER.getValue());
        }
        ctx.clearToken();
        return true;
    }

    static Symbol ageSymbol = new Symbol("age");
    static Symbol dadSymbol = new Symbol("dad");
    static Symbol intMapSymbol = new Symbol("intMap");
    static Symbol kidsSymbol = new Symbol("kids");
    static Symbol marriedSymbol = new Symbol("married");
    static Symbol moneySymbol = new Symbol("money");
    static Symbol nameSymbol = new Symbol("name");
    static Symbol petsSymbol = new Symbol("pets");
    static Symbol siblingsSymbol = new Symbol("siblings");

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
        int value = ctx.popIntToken();
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
        person.setMarried(ctx.popBooleanToken());
    };
}
