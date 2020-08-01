package io.quarkus.json.parser.test;

import io.quarkus.json.parser.CollectionParser;
import io.quarkus.json.parser.ContextValue;
import io.quarkus.json.parser.IntChar;
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

    protected boolean check(String str, ParserContext ctx) {
        for (int i = 0; i < str.length(); i++) {
            int c = ctx.consume();
            if (c == IntChar.INT_QUOTE) return false;
            if (c != str.charAt(i)) return false;
        }
        if (ctx.consume() != IntChar.INT_QUOTE) {
            ctx.skipToQuote();
            return false;
        }
        return true;
    }

    protected boolean handleKey(ParserContext ctx) {
        int c = ctx.consume();
        char ch = (char)c;

        if (c == 'a') {
            if (!check("ge", ctx)) return false;
            ctx.clearToken();
            valueSeparator(ctx);
            startIntegerValue(ctx);
            int value = ctx.popIntToken();
            Person person = ctx.target();
            person.setAge(value);
            return true;
        } else if (c == 'm') {
            c = ctx.consume();
            if (c == 'a') {
                if (!check("rried", ctx)) return false;
                ctx.clearToken();
                valueSeparator(ctx);
                startBooleanValue(ctx);
                boolean value = ctx.popBooleanToken();
                Person person = ctx.target();
                person.setMarried(value);
                return true;
            } else if (c == 'o') {
                if (!check("ney", ctx)) return false;
                ctx.clearToken();
                valueSeparator(ctx);
                startNumberValue(ctx);
                float value = Float.parseFloat(ctx.popToken());
                Person person = ctx.target();
                person.setMoney(value);
                return true;
            } else if (c != IntChar.INT_QUOTE) {
                ctx.skipToQuote();
            }
        } else if (c == 'n') {
            if (!check("ame", ctx)) return false;
            ctx.clearToken();
            valueSeparator(ctx);
            startStringValue(ctx);
            Person person = ctx.target();
            person.setName(ctx.popToken());
            return true;
        } else if (c == 'i') {
            if (!check("ntMap", ctx)) return false;
            ctx.clearToken();
            ctx.pushTarget(new HashMap());
            valueSeparator(ctx);
            intMapStart.parse(ctx);
            Map<String, Integer> intMap = ctx.popTarget();
            Person person = ctx.target();
            person.setIntMap(intMap);
            return true;
        } else if (c == 'k') {
            if (!check("ids", ctx)) return false;
            ctx.clearToken();
            ctx.pushTarget(new HashMap());
            valueSeparator(ctx);
            kidsStart.parse(ctx);
            Map<String, Person> kids = ctx.popTarget();
            Person person = ctx.target();
            person.setKids(kids);
            return true;
        } else if (c == 'd') {
            if (!check("ad", ctx)) return false;
            ctx.clearToken();
            valueSeparator(ctx);
            ExamplePersonParser.PARSER.getStart().parse(ctx);
            Person dad = ctx.popTarget();
            Person person = ctx.target();
            person.setDad(dad);
            return true;
        } else if (c == 'p') {
            if (!check("ets", ctx)) return false;
            ctx.clearToken();
            ctx.pushTarget(new LinkedList());
            valueSeparator(ctx);
            petsStart.parse(ctx);
            List<String> pets = ctx.popTarget();
            Person person = ctx.target();
            person.setPets(pets);
           return true;
        } else if (c == 's') {
            if (!check("iblings", ctx)) return false;
            ctx.clearToken();
            ctx.pushTarget(new LinkedList<>());
            valueSeparator(ctx);
            siblingsStart.parse(ctx);
            List<Person> siblings = ctx.popTarget();
            Person person = ctx.target();
            person.setSiblings(siblings);
            return true;
        } else if (c != IntChar.INT_QUOTE) {
            ctx.skipToQuote();
        }
        return false;
    }

    @Override
    public void key(ParserContext ctx) {
        if (!handleKey(ctx)) {
            valueSeparator(ctx);
            SkipParser.PARSER.unpushedValue(ctx);
        }
    }
}
