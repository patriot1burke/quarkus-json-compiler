package io.quarkus.json.parser.test;

import io.quarkus.json.parser.CollectionParser;
import io.quarkus.json.parser.ContextValue;
import io.quarkus.json.parser.MapParser;
import io.quarkus.json.parser.ObjectParser;
import io.quarkus.json.parser.ParserContext;
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
            ctx.pushState(this::nameEnd);
            ctx.pushState(this::startStringValue);
        } else if (key.equals("age")) {
            ctx.pushState(this::ageEnd);
            ctx.pushState(this::startNumberValue);
        } else if (key.equals("money")) {
            ctx.pushState(this::moneyEnd);
            ctx.pushState(this::startNumberValue);
        } else if (key.equals("married")) {
            ctx.pushState(this::marriedEnd);
            ctx.pushState(this::startBooleanValue);
        } else if (key.equals("intMap")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(this::intMapEnd);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
                    ObjectParser.PARSER::startIntegerValue)::start);
        }
        else if (key.equals("dad")) {
            ctx.pushState(this::dadEnd);
            ctx.pushState(PersonParser.PARSER::start);
        } else if (key.equals("kids")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(this::kidsEnd);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE,
                    ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER::start)::start);

        } else if (key.equals("siblings")) {
            ctx.pushTarget(new LinkedList<>());
            ctx.pushState(this::siblingsEnd);
            ctx.pushState(new CollectionParser(ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER::start)::start);
        } else if (key.equals("pets")) {
            ctx.pushTarget(new LinkedList());
            ctx.pushState(this::petsEnd);
            ctx.pushState(new CollectionParser(ContextValue.STRING_VALUE,
                    ObjectParser.PARSER::startStringValue)::start);
        }
        else {
            ctx.pushState(SkipParser.PARSER::value);
        }
        return true;
    }

    public void siblingsEnd(ParserContext ctx) {
        ctx.popState();
        List<Person> siblings = ctx.popTarget();
        Person person = ctx.target();
        person.setSiblings(siblings);
    }

    public void petsEnd(ParserContext ctx) {
        ctx.popState();
        List<String> pets = ctx.popTarget();
        Person person = ctx.target();
        person.setPets(pets);
    }

    public void kidsEnd(ParserContext ctx) {
        ctx.popState();
        Map<String, Person> kids = ctx.popTarget();
        Person person = ctx.target();
        person.setKids(kids);
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
