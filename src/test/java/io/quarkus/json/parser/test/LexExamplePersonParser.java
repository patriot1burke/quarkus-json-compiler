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

public class LexExamplePersonParser extends ObjectParser {

    public static final LexExamplePersonParser PARSER = new LexExamplePersonParser();

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
    public void key(ParserContext ctx) {
        ctx.startToken(0);
        ctx.skipToQuote();
        ctx.endToken();
        if (ctx.tokenEquals(ageSymbol)) {
            ctx.clearToken();
            valueSeparator(ctx);
            startIntegerValue(ctx);
            int value = ctx.popIntToken();
            Person person = ctx.target();
            person.setAge(value);
        } else if (ctx.tokenEquals(marriedSymbol)) {
            ctx.clearToken();
            valueSeparator(ctx);
            startBooleanValue(ctx);
            boolean value = ctx.popBooleanToken();
            Person person = ctx.target();
            person.setMarried(value);
        } else if (ctx.tokenEquals(moneySymbol)) {
            ctx.clearToken();
            valueSeparator(ctx);
            startNumberValue(ctx);
            float value = Float.parseFloat(ctx.popToken());
            Person person = ctx.target();
            person.setMoney(value);
        } else if (ctx.tokenEquals(nameSymbol)) {
            ctx.clearToken();
            valueSeparator(ctx);
            startStringValue(ctx);
            Person person = ctx.target();
            person.setName(ctx.popToken());
        } else if (ctx.tokenEquals(dadSymbol)) {
            ctx.clearToken();
            valueSeparator(ctx);
            LexExamplePersonParser.PARSER.getStart().parse(ctx);
            Person dad = ctx.popTarget();
            Person person = ctx.target();
            person.setDad(dad);
        } else if (ctx.tokenEquals(intMapSymbol)) {
            ctx.clearToken();
            ctx.pushTarget(new HashMap());
            valueSeparator(ctx);
            intMapStart.parse(ctx);
            Map<String, Integer> intMap = ctx.popTarget();
            Person person = ctx.target();
            person.setIntMap(intMap);
        } else if (ctx.tokenEquals(kidsSymbol)) {
            ctx.clearToken();
            ctx.pushTarget(new HashMap());
            valueSeparator(ctx);
            kidsStart.parse(ctx);
            Map<String, Person> kids = ctx.popTarget();
            Person person = ctx.target();
            person.setKids(kids);
        } else if (ctx.tokenEquals(petsSymbol)) {
            ctx.clearToken();
            ctx.pushTarget(new LinkedList());
            valueSeparator(ctx);
            petsStart.parse(ctx);
            List<String> pets = ctx.popTarget();
            Person person = ctx.target();
            person.setPets(pets);
        } else if (ctx.tokenEquals(siblingsSymbol)) {
            ctx.clearToken();
            ctx.pushTarget(new LinkedList<>());
            valueSeparator(ctx);
            siblingsStart.parse(ctx);
            List<Person> siblings = ctx.popTarget();
            Person person = ctx.target();
            person.setSiblings(siblings);
        } else {
            String key = ctx.popToken();
            valueSeparator(ctx);
            try {
                SkipParser.PARSER.unpushedValue(ctx);
            } catch (RuntimeException e) {
                throw new RuntimeException("Failed on skipped key " + key, e);
            }
        }
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
}
