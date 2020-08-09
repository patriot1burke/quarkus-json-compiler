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

    private MapParser intMap = new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
            ObjectParser.PARSER.getStartIntegerValue(), ObjectParser.PARSER.getContinueStartIntegerValue());
    private MapParser kids = new MapParser(ContextValue.STRING_VALUE,
            ContextValue.OBJECT_VALUE,
            getStart(), getContinueStart());
    private CollectionParser siblings = new CollectionParser(ContextValue.OBJECT_VALUE,
            getStart());
    private CollectionParser pets = new CollectionParser(ContextValue.STRING_VALUE,
            ObjectParser.PARSER.getStartStringValue());

    @Override
    public void beginObject(ParserContext ctx) {
        ctx.pushTarget(new Person());
    }

    @Override
    public boolean key(ParserContext ctx) {
        int c = ctx.skipToQuote();
        if (c == 0) {
            ctx.pushState(getContinueKey());
            return false;
        }
        ctx.endToken();
        int index = 0;
        c = ctx.tokenCharAt(index++);
        char ch = (char)c;
        int stateIndex = ctx.stateIndex();
        if (c == 'a') {
            if (ctx.compareToken(index, "ge")) {
                if (!valueSeparator(ctx)) {
                    ctx.pushState(getContinueStartIntegerValue(), stateIndex);
                    ctx.pushState(ageEnd, stateIndex);
                    return false;
                }
                if (!startIntegerValue(ctx)) {
                    ctx.pushState(ageEnd, stateIndex);
                    return false;
                }
                return ageEnd(ctx);
            }
        } else if (c == 'm') {
            c = ctx.tokenCharAt(index++);
            if (c == 'a') {
                if (ctx.compareToken(index, "rried")) {
                    if (!valueSeparator(ctx)) {
                        ctx.pushState(getContinueStartBooleanValue(), stateIndex);
                        ctx.pushState(marriedEnd, stateIndex);
                        return false;
                    }
                    if (!startBooleanValue(ctx)) {
                        ctx.pushState(marriedEnd, stateIndex);
                        return false;
                    }
                    return marriedEnd(ctx);
                }
            } else if (c == 'o') {
                if (ctx.compareToken(index,"ney")) {
                    if (!valueSeparator(ctx)) {
                        ctx.pushState(getContinueStartNumberValue(), stateIndex);
                        ctx.pushState(moneyEnd, stateIndex);
                        return false;
                    }
                    if (!startNumberValue(ctx)) {
                        ctx.pushState(moneyEnd, stateIndex);
                        return false;
                    }
                    return moneyEnd(ctx);
                }
            }
        } else if (c == 'n') {
            if (ctx.compareToken(index,"ame")) {
                if (!valueSeparator(ctx)) {
                    ctx.pushState(getContinueStartStringValue(), stateIndex);
                    ctx.pushState(nameEnd, stateIndex);
                    return false;
                }
                if (!startStringValue(ctx)) {
                    ctx.pushState(nameEnd, stateIndex);
                    return false;
                }
                return nameEnd(ctx);
            }
        } else if (c == 'i') {
            if (ctx.compareToken(index,"ntMap")) {
                ctx.pushTarget(new HashMap());
                if (!valueSeparator(ctx)) {
                    ctx.pushState(intMap.getContinueStart(), stateIndex);
                    ctx.pushState(intMapEnd, stateIndex);
                    return false;
                }
                if (!intMap.start(ctx)) {
                    ctx.pushState(intMapEnd, stateIndex);
                    return false;
                }
                return intMapEnd(ctx);
            }
        } else if (c == 'k') {
            if (ctx.compareToken(index,"ids")) {
                ctx.pushTarget(new HashMap());
                if (!valueSeparator(ctx)) {
                    ctx.pushState(kids.getContinueStart(), stateIndex);
                    ctx.pushState(kidsEnd, stateIndex);
                    return false;
                }
                if (!kids.start(ctx)) {
                    ctx.pushState(kidsEnd, stateIndex);
                    return false;
                }
                return kidsEnd(ctx);
            }
        } else if (c == 'd') {
            if (ctx.compareToken(index,"ad")) {
                if (!valueSeparator(ctx)) {
                    ctx.pushState(NioExamplePersonParser.PARSER.getContinueStart(), stateIndex);
                    ctx.pushState(dadEnd, stateIndex);
                    return false;
                }
                if (!NioExamplePersonParser.PARSER.getStart().parse(ctx)) {
                    ctx.pushState(dadEnd, stateIndex);
                    return false;
                }
                return dadEnd(ctx);
            }
        } else if (c == 'p') {
            if (ctx.compareToken(index,"ets")) {
                ctx.pushTarget(new LinkedList());
                if (!valueSeparator(ctx)) {
                    ctx.pushState(pets.getContinueStart(), stateIndex);
                    ctx.pushState(petsEnd, stateIndex);
                    return false;
                }
                if (!pets.start(ctx)) {
                    ctx.pushState(petsEnd, stateIndex);
                    return false;
                }
                return petsEnd(ctx);
            }
        } else if (c == 's') {
            if (ctx.compareToken(index,"iblings")) {
                ctx.pushTarget(new LinkedList<>());
                if (!valueSeparator(ctx)) {
                    ctx.pushState(siblings.getContinueStart(), stateIndex);
                    ctx.pushState(siblingsEnd, stateIndex);
                    return false;
                }
                if (!siblings.start(ctx)) {
                    ctx.pushState(siblingsEnd, stateIndex);
                    return false;
                }
                return siblingsEnd(ctx);
            }
        }
        return SkipParser.PARSER.skipValue(ctx);
    }

    static ParserState siblingsEnd = (ctx) -> {
        ctx.popState();
        return siblingsEnd(ctx);
    };

    private static boolean siblingsEnd(ParserContext ctx) {
        List<Person> siblings = ctx.popTarget();
        Person person = ctx.target();
        person.setSiblings(siblings);
        return true;
    }

    static ParserState petsEnd = (ctx) -> {
        ctx.popState();
        return petsEnd(ctx);
    };

    private static boolean petsEnd(ParserContext ctx) {
        List<String> pets = ctx.popTarget();
        Person person = ctx.target();
        person.setPets(pets);
        return true;
    }

    static ParserState dadEnd = (ctx) -> {
        ctx.popState();
        return dadEnd(ctx);
    };

    private static boolean dadEnd(ParserContext ctx) {
        Person dad = ctx.popTarget();
        Person person = ctx.target();
        person.setDad(dad);
        return true;
    }

    static ParserState kidsEnd = (ctx) -> {
        ctx.popState();
        return kidsEnd(ctx);
    };

    private static boolean kidsEnd(ParserContext ctx) {
        Map<String, Person> kids = ctx.popTarget();
        Person person = ctx.target();
        person.setKids(kids);
        return true;
    }

    static ParserState intMapEnd = (ctx) -> {
        ctx.popState();
        return intMapEnd(ctx);
    };

    private static boolean intMapEnd(ParserContext ctx) {
        Map<String, Integer> intMap = ctx.popTarget();
        Person person = ctx.target();
        person.setIntMap(intMap);
        return true;
    }

    static ParserState nameEnd = (ctx) -> {
        ctx.popState();
        return nameEnd(ctx);
    };

    private static boolean nameEnd(ParserContext ctx) {
        Person person = ctx.target();
        person.setName(ctx.popToken());
        return true;
    }

    static ParserState moneyEnd = (ctx) -> {
        ctx.popState();
        return moneyEnd(ctx);
    };

    private static boolean moneyEnd(ParserContext ctx) {
        float value = ctx.popFloatToken();
        Person person = ctx.target();
        person.setMoney(value);
        return true;
    }

    static ParserState marriedEnd = (ctx) -> {
        ctx.popState();
        return marriedEnd(ctx);
    };

    private static boolean marriedEnd(ParserContext ctx) {
        boolean value = ctx.popBooleanToken();
        Person person = ctx.target();
        person.setMarried(value);
        return true;
    }

    static ParserState ageEnd = (ctx) -> {
        ctx.popState();
        return ageEnd(ctx);
    };

    private static boolean ageEnd(ParserContext ctx) {
        int value = ctx.popIntToken();
        Person person = ctx.target();
        person.setAge(value);
        return true;
    }
}
