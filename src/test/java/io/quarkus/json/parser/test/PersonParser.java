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
            ctx.pushState(nameEnd.INSTANCE);
            ctx.pushState(getStartStringValue());
        } else if (key.equals("age")) {
            ctx.pushState(ageEnd.INSTANCE);
            ctx.pushState(getStartNumberValue());
        } else if (key.equals("money")) {
            ctx.pushState(moneyEnd.INSTANCE);
            ctx.pushState(getStartNumberValue());
        } else if (key.equals("married")) {
            ctx.pushState(marriedEnd.INSTANCE);
            ctx.pushState(getStartBooleanValue());
        } else if (key.equals("intMap")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(intMapEnd.INSTANCE);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE, ContextValue.INT_VALUE,
                    ObjectParser.PARSER.getStartIntegerValue()).getStart());
        }
        else if (key.equals("dad")) {
            ctx.pushState(dadEnd.INSTANCE);
            ctx.pushState(PersonParser.PARSER::start);
        } else if (key.equals("kids")) {
            ctx.pushTarget(new HashMap());
            ctx.pushState(kidsEnd.INSTANCE);
            ctx.pushState(new MapParser(ContextValue.STRING_VALUE,
                    ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER.getStart()).getStart());

        } else if (key.equals("siblings")) {
            ctx.pushTarget(new LinkedList<>());
            ctx.pushState(siblingsEnd.INSTANCE);
            ctx.pushState(new CollectionParser(ContextValue.OBJECT_VALUE,
                    PersonParser.PARSER.getStart()).getStart());
        } else if (key.equals("pets")) {
            ctx.pushTarget(new LinkedList());
            ctx.pushState(petsEnd.INSTANCE);
            ctx.pushState(new CollectionParser(ContextValue.STRING_VALUE,
                    ObjectParser.PARSER.getStartStringValue()).getStart());
        }
        else {
            ctx.pushState(SkipParser.PARSER.getValue());
        }
        return true;
    }

    static class siblingsEnd implements ParserState {
        public static final siblingsEnd INSTANCE=new siblingsEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            List<Person> siblings = ctx.popTarget();
            Person person = ctx.target();
            person.setSiblings(siblings);
        }
    }

    static class petsEnd implements ParserState {
        public static final petsEnd INSTANCE = new petsEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            List<String> pets = ctx.popTarget();
            Person person = ctx.target();
            person.setPets(pets);
        }
    }

    static class kidsEnd implements ParserState {
        public static final kidsEnd INSTANCE = new kidsEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Map<String, Person> kids = ctx.popTarget();
            Person person = ctx.target();
            person.setKids(kids);
        }
    }

    static class dadEnd implements ParserState {
        public static final dadEnd INSTANCE = new dadEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Person dad = ctx.popTarget();
            Person person = ctx.target();
            person.setDad(dad);
        }
    }

    static class intMapEnd implements ParserState {
        public static final intMapEnd INSTANCE = new intMapEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Map<String, Integer> intMap = ctx.popTarget();
            Person person = ctx.target();
            person.setIntMap(intMap);
        }
    }

    static class nameEnd implements ParserState {
        public static final nameEnd INSTANCE = new nameEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Person person = ctx.target();
            person.setName(ctx.popToken());
        }
    }

    static class ageEnd implements ParserState {
        public static final ageEnd INSTANCE = new ageEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Integer value = Integer.valueOf(ctx.popToken());
            Person person = ctx.target();
            person.setAge(value);
        }
    }

    static class moneyEnd implements ParserState {
        public static final moneyEnd INSTANCE = new moneyEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Float value = Float.valueOf(ctx.popToken());
            Person person = ctx.target();
            person.setMoney(value);
        }
    }

    static class marriedEnd implements ParserState {
        public static final marriedEnd INSTANCE = new marriedEnd();
        @Override
        public void parse(ParserContext ctx) {
            ctx.popState();
            Person person = ctx.target();
            person.setMarried(Boolean.parseBoolean(ctx.popToken()));
        }
    }
}
