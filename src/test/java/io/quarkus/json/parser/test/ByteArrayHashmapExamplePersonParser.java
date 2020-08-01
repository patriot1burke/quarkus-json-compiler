package io.quarkus.json.parser.test;

import io.quarkus.json.parser.ByteArrayKey;
import io.quarkus.json.parser.CollectionParser;
import io.quarkus.json.parser.ContextValue;
import io.quarkus.json.parser.MapParser;
import io.quarkus.json.parser.ObjectParser;
import io.quarkus.json.parser.ParserContext;
import io.quarkus.json.parser.ParserState;
import io.quarkus.json.parser.SkipParser;
import io.quarkus.json.parser.Symbol;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class ByteArrayHashmapExamplePersonParser extends ObjectParser {

    public static final ByteArrayHashmapExamplePersonParser PARSER = new ByteArrayHashmapExamplePersonParser();

    private ParserState intMapStart = new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
            ObjectParser.PARSER.getStartIntegerValue()).getStart();
    private ParserState kidsStart = new MapParser(ContextValue.STRING_VALUE,
            ContextValue.OBJECT_VALUE,
            getStart()).getStart();
    private ParserState siblingsStart = new CollectionParser(ContextValue.OBJECT_VALUE,
            getStart()).getStart();
    private ParserState petsStart = new CollectionParser(ContextValue.STRING_VALUE,
            ObjectParser.PARSER.getStartStringValue()).getStart();

    private HashMap<ByteArrayKey, ParserState> state = new HashMap<>();

    public ByteArrayHashmapExamplePersonParser() {
        try {
            state.put(new ByteArrayKey("age".getBytes("UTF-8")), this::age);
            state.put(new ByteArrayKey("dad".getBytes("UTF-8")), this::dad);
            state.put(new ByteArrayKey("intMap".getBytes("UTF-8")), this::intMap);
            state.put(new ByteArrayKey("kids".getBytes("UTF-8")), this::kids);
            state.put(new ByteArrayKey("married".getBytes("UTF-8")), this::married);
            state.put(new ByteArrayKey("money".getBytes("UTF-8")), this::money);
            state.put(new ByteArrayKey("name".getBytes("UTF-8")), this::name);
            state.put(new ByteArrayKey("pets".getBytes("UTF-8")), this::pets);
            state.put(new ByteArrayKey("siblings".getBytes("UTF-8")), this::siblings);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new Person());
    }

    @Override
    public boolean handleKey(ParserContext ctx) {
        ParserState state = this.state.get(ctx.tokenKey());
        ctx.clearToken();
        if (state == null) {
            ctx.pushState(SkipParser.PARSER.getValue());
        } else {
            state.parse(ctx);
        }
        return true;
    }

    public void siblings(ParserContext ctx) {
        ctx.pushTarget(new LinkedList<>());
        ctx.pushState(siblingsEnd);
        ctx.pushState(siblingsStart);
    }

    public void pets(ParserContext ctx) {
        ctx.pushTarget(new LinkedList());
        ctx.pushState(petsEnd);
        ctx.pushState(petsStart);
    }

    public void name(ParserContext ctx) {
        ctx.pushState(nameEnd);
        ctx.pushState(getStartStringValue());
    }

    public void money(ParserContext ctx) {
        ctx.pushState(moneyEnd);
        ctx.pushState(getStartNumberValue());
    }

    public void married(ParserContext ctx) {
        ctx.pushState(marriedEnd);
        ctx.pushState(getStartBooleanValue());
    }

    public void kids(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
        ctx.pushState(kidsEnd);
        ctx.pushState(kidsStart);
    }

    public void intMap(ParserContext ctx) {
        ctx.pushTarget(new HashMap());
        ctx.pushState(intMapEnd);
        ctx.pushState(intMapStart);
    }

    public void dad(ParserContext ctx) {
        ctx.pushState(dadEnd);
        ctx.pushState(ByteArrayHashmapExamplePersonParser.PARSER.getStart());
    }

    public void age(ParserContext ctx) {
        ctx.pushState(ageEnd);
        ctx.pushState(getStartNumberValue());
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
