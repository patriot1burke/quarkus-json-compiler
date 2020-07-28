package io.quarkus.json.parser;

public interface ContextValue {
    Object value(ParserContext ctx);

    ContextValue BYTE_VALUE = (ctx) -> Byte.valueOf(ctx.popToken());
    ContextValue BOOLEAN_VALUE = (ctx) -> Boolean.valueOf(ctx.popToken());
    ContextValue INT_VALUE = (ctx) -> Integer.valueOf(ctx.popToken());
    ContextValue SHORT_VALUE = (ctx) -> Short.valueOf(ctx.popToken());
    ContextValue LONG_VALUE = (ctx) -> Long.valueOf(ctx.popToken());
    ContextValue FLOAT_VALUE = (ctx) -> Float.valueOf(ctx.popToken());
    ContextValue DOUBLE_VALUE = (ctx) -> Double.valueOf(ctx.popToken());
    ContextValue STRING_VALUE = (ctx) -> ctx.popToken();
    ContextValue CHAR_VALUE = (ctx) -> {
        String val = ctx.popToken();
        if (val.length() != 1) throw new RuntimeException("Expecting single character for value at character " + ctx.charCount());
        return val.charAt(0);
    };
}
