package io.quarkus.json.deserializer.nio;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GenericSetParser extends BaseParser implements JsonParser {

    public static final GenericSetParser PARSER = new GenericSetParser();

    @Override
    public void beginList(ParserContext ctx) {
        ctx.pushTarget(new HashSet());
    }

}
