package io.quarkus.json.parser.test;

import io.quarkus.json.nio.CollectionParser;
import io.quarkus.json.nio.ContextValue;
import io.quarkus.json.nio.MapParser;
import io.quarkus.json.nio.ObjectParser;
import io.quarkus.json.nio.ParserContext;
import io.quarkus.json.nio.ParserState;
import io.quarkus.json.nio.SkipParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class NioExamplePersonParser extends ObjectParser {

    public static final NioExamplePersonParser PARSER = new NioExamplePersonParser();

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
    public boolean key(ParserContext ctx) {
        int c = ctx.consume();

        if (c == 'a') {
            if (ctx.check("ge")) {
                valueSeparator(ctx);
                startIntegerValue(ctx);
                int value = ctx.popIntToken();
                Person person = ctx.target();
                person.setAge(value);
                return true;
            }
        } else if (c == 'm') {
            c = ctx.consume();
            if (c == 'a') {
                if (ctx.check("rried")) {
                    valueSeparator(ctx);
                    startBooleanValue(ctx);
                    boolean value = ctx.popBooleanToken();
                    Person person = ctx.target();
                    person.setMarried(value);
                    return true;
                }
            } else if (c == 'o') {
                if (ctx.check("ney")) {
                    valueSeparator(ctx);
                    startNumberValue(ctx);
                    float value = Float.parseFloat(ctx.popToken());
                    Person person = ctx.target();
                    person.setMoney(value);
                    return true;
                }
            }
        } else if (c == 'n') {
            if (ctx.check("ame")) {
                valueSeparator(ctx);
                startStringValue(ctx);
                Person person = ctx.target();
                person.setName(ctx.popToken());
                return true;
            }
        } else if (c == 'i') {
            if (ctx.check("ntMap")) {
                ctx.pushTarget(new HashMap());
                valueSeparator(ctx);
                intMapStart.parse(ctx);
                Map<String, Integer> intMap = ctx.popTarget();
                Person person = ctx.target();
                person.setIntMap(intMap);
                return true;
            }
        } else if (c == 'k') {
            if (ctx.check("ids")) {
                ctx.pushTarget(new HashMap());
                valueSeparator(ctx);
                kidsStart.parse(ctx);
                Map<String, Person> kids = ctx.popTarget();
                Person person = ctx.target();
                person.setKids(kids);
                return true;
            }
        } else if (c == 'd') {
            if (ctx.check("ad")) {
                valueSeparator(ctx);
                NioExamplePersonParser.PARSER.getStart().parse(ctx);
                Person dad = ctx.popTarget();
                Person person = ctx.target();
                person.setDad(dad);
                return true;
            }
        } else if (c == 'p') {
            if (ctx.check("ets")) {
                ctx.pushTarget(new LinkedList());
                valueSeparator(ctx);
                petsStart.parse(ctx);
                List<String> pets = ctx.popTarget();
                Person person = ctx.target();
                person.setPets(pets);
                return true;
            }
        } else if (c == 's') {
            if (ctx.check("iblings")) {
                ctx.pushTarget(new LinkedList<>());
                valueSeparator(ctx);
                siblingsStart.parse(ctx);
                List<Person> siblings = ctx.popTarget();
                Person person = ctx.target();
                person.setSiblings(siblings);
                return true;
            }
        }
        ctx.skipToQuote();
        valueSeparator(ctx);
        SkipParser.PARSER.value(ctx);
        return true;
    }
}
