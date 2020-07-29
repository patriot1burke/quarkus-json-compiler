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
            ctx.pushState(nameEnd);
            ctx.pushState(getStartStringValue());
        } else if (key.equals("age")) {
            ctx.pushState(ageEnd);
            ctx.pushState(getStartNumberValue());
        } else if (key.equals("money")) {
            ctx.pushState(moneyEnd);
            ctx.pushState(getStartNumberValue());
        } else if (key.equals("married")) {
            ctx.pushState(marriedEnd);
            ctx.pushState(getStartBooleanValue());
        } else if (key.equals("intMap")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(intMapEnd);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
                    ObjectParser.PARSER.getStartIntegerValue()).getStart());
        }
        else if (key.equals("dad")) {
            ctx.pushState(dadEnd);
            ctx.pushState(PersonParser.PARSER.getStart());
        } else if (key.equals("kids")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(kidsEnd);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE,
                    ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER.getStart()).getStart());

        } else if (key.equals("siblings")) {
            ctx.pushTarget(new LinkedList<>());
            ctx.pushState(siblingsEnd);
            ctx.pushState(new CollectionParser(ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER.getStart()).getStart());
        } else if (key.equals("pets")) {
            ctx.pushTarget(new LinkedList());
            ctx.pushState(petsEnd);
            ctx.pushState(new CollectionParser(ContextValue.STRING_VALUE,
                    ObjectParser.PARSER.getStartStringValue()).getStart());
        }
        else {
            ctx.pushState(SkipParser.PARSER.getValue());
        }
        return true;
    }

    ParserState siblingsEnd = (ctx) -> {
        ctx.popState();
        List<Person> siblings = ctx.popTarget();
        Person person = ctx.target();
        person.setSiblings(siblings);
    };

    ParserState petsEnd = (ctx) -> {
        ctx.popState();
        List<String> pets = ctx.popTarget();
        Person person = ctx.target();
        person.setPets(pets);
    };

    ParserState kidsEnd = (ctx) -> {
        ctx.popState();
        Map<String, Person> kids = ctx.popTarget();
        Person person = ctx.target();
        person.setKids(kids);
    };

    ParserState dadEnd = (ctx) -> {
        ctx.popState();
        Person dad = ctx.popTarget();
        Person person = ctx.target();
        person.setDad(dad);
    };

    ParserState intMapEnd = (ctx) -> {
        ctx.popState();
        Map<String, Integer> intMap = ctx.popTarget();
        Person person = ctx.target();
        person.setIntMap(intMap);
    };

    ParserState nameEnd = (ctx) -> {
        ctx.popState();
        Person person = ctx.target();
        person.setName(ctx.popToken());
    };

    ParserState ageEnd = (ctx) -> {
        ctx.popState();
        Integer value = Integer.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setAge(value);
    };

    ParserState moneyEnd = (ctx) -> {
        ctx.popState();
        Float value = Float.valueOf(ctx.popToken());
        Person person = ctx.target();
        person.setMoney(value);
    };

    ParserState marriedEnd = (ctx) -> {
        ctx.popState();
        Person person = ctx.target();
        person.setMarried(Boolean.parseBoolean(ctx.popToken()));
    };
}
