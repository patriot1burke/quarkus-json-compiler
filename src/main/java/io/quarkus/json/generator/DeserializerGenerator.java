package io.quarkus.json.generator;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class DeserializerGenerator {
    private PrintWriter writer;
    int indent;
    Set<Class> needDeserializerFor;
    Class targetType;
    Type targetGenericType;

    public DeserializerGenerator(PrintWriter writer, Set<Class> needDeserializerFor, Class targetType, Type targetGenericType) {
        this.writer = writer;
        this.needDeserializerFor = needDeserializerFor;
        this.targetType = targetType;
        this.targetGenericType = targetGenericType;
    }

    void indent() {
        for (int i = 0; i < indent; i++) {
            writer.print("    ");
        }
    }
    void indent(String text) {
        indent();
        writer.print(text);
    }
    void indentln(String line) {
        indent();
        writer.println(line);
    }

    void println(String... lines) {
        for (String line : lines) writer.println(line);
    }

    void print(String text) {
        writer.print(text);
    }

    static boolean isSetter(Method m) {
        return !Modifier.isStatic(m.getModifiers()) && m.getName().startsWith("set") && m.getName().length() > "set".length()
                && m.getParameterCount() == 1;
    }
    class Setter {
        String name;
        Method method;
        Class type;
        Type genericType;

        public Setter(String name, Method method, Class type, Type genericType) {
            this.name = name;
            this.method = method;
            this.type = type;
            this.genericType = genericType;
        }
    }

    public static String name(Class clz, Type genericType) {
        return clz.getSimpleName() + "__Parser";
    }

    public static String fqn(Class clz, Type genericType) {
        return clz.getName() + "__Parser";
    }

    List<Setter> setters = new LinkedList<>();

    public void generate() {
        String parser = name(targetType, targetGenericType);
        findSetters(targetType);

        indentln("package " + targetType.getPackage().getName() + ";");
        indentln("import io.quarkus.json.parser.*;");
        println();
        indentln("public class " + parser + " extends ObjectParser {");
        indent++;
        println();
        indentln("public static final " + parser + " PARSER = new " + parser + "();");
        println();
        indentln("@Override");
        indentln("public void beginObject(ParserContext ctx) {");
        indent++;
        indentln("ctx.pushTarget(new " + targetType.getName() + "());");
        indent--;
        indentln("}");
        println();
        indentln("@Override");
        indentln("public boolean handleKey(ParserContext ctx) {");
        indent++;
        indentln("String key = ctx.popToken();");
        if (setters.size() == 0) {
            indentln("ctx.pushState(SkipParser.PARSER::value);");
        } else {
            boolean first = true;
            for (Setter setter : setters) {
                if (first) {
                    first = false;
                    indent();
                } else {
                    print(" else ");
                }
                println("if (key.equals(\"" + setter.name + "\")) {");
                indent++;
                outputSetter(setter);
                indent--;
                indent("}");
            }
            println(" else {");
            indent++;
            indentln("ctx.pushState(SkipParser.PARSER::value);");
            indent--;
            indentln("}");
        }
        indentln("return true;");
        indent--;
        indentln("}");
        println();
        println();
        for (Setter setter : setters) {
            indentln("ParserState " + setter.name + "End = (ctx) -> {");
            indent++;
            indentln("ctx.popState();");
            popValue(setter.type, setter.genericType);
            indentln(targetGenericType.getTypeName() + " target = ctx.target();");
            indentln("target." + setter.method.getName() + "(value);");
            indent--;
            indentln("};");
        }
        indent--;
        println("}");
    }

    public void popValue(Class clz, Type type) {
        if (String.class.equals(clz)) {
            indentln("String value = ctx.popToken();");
        } else if (clz.equals(char.class)) {
            indentln("String tmp = ctx.popToken();");
            indentln("if (tmp.length() != 1) throw new RuntimeException(\"Expected character not string\");");
            indentln("char value = tmp.charAt(0);");
        } else if (clz.equals(Character.class)) {
            indentln("String tmp = ctx.popToken()");
            indentln("if (tmp.length() != 1) throw new RuntimeException(\"Expected character not string\");");
            indentln("Character value = tmp.charAt(0);");
        } else if (clz.equals(OffsetDateTime.class)) {
            indentln("java.time.OffsetDateTime value = java.time.OffsetDateTime.parse(ctx.popToken());");
        } else if (clz.equals(long.class)) {
            indentln("long value = Long.parseLong(ctx.popToken());");
        } else if (clz.equals(Long.class)) {
            indentln("Long value = Long.valueOf(ctx.popToken());");
        } else if (clz.equals(int.class)) {
            indentln("int value = Integer.parseInt(ctx.popToken());");
        } else if (clz.equals(Integer.class)) {
            indentln("Integer value = Integer.valueOf(ctx.popToken());");
        } else if (clz.equals(short.class)) {
            indentln("short value = Short.parseShort(ctx.popToken());");
        } else if (clz.equals(Short.class)) {
            indentln("Short value = Short.valueOf(ctx.popToken());");
        } else if (clz.equals(byte.class)) {
            indentln("byte value = Byte.parseByte(ctx.popToken());");
        } else if (clz.equals(Byte.class)) {
            indentln("Byte value = Byte.valueOf(ctx.popToken());");
        } else if (clz.equals(float.class)) {
            indentln("float value = Float.parseFloat(ctx.popToken());");
        } else if (clz.equals(Float.class)) {
            indentln("Float value = Float.valueOf(ctx.popToken());");
        } else if (clz.equals(double.class)) {
            indentln("double value = Double.parseDouble(ctx.popToken());");
        } else if (clz.equals(Double.class)) {
            indentln("Double value = Double.valueOf(ctx.popToken());");
        } else if (clz.equals(boolean.class)) {
            indentln("boolean value = Boolean.parseBoolean(ctx.popToken());");
        } else if (clz.equals(Boolean.class)) {
            indentln("Boolean value = Boolean.valueOf(ctx.popToken());");
        } else if (clz.equals(Object.class)) {
            indentln("Object value = ctx.popTarget();");
        } else {
            indentln(type.getTypeName() + " value = ctx.popTarget();");
        }
    }
    public void outputSetter(Setter setter) {
        Class clz = setter.type;
        indentln("ctx.pushState(" + setter.name + "End);");
        if (String.class.equals(clz)
                || clz.equals(char.class) || clz.equals(Character.class)
                || clz.equals(OffsetDateTime.class)
        ) {
            indentln("ctx.pushState(getStartStringValue());");
        } else if (clz.equals(long.class) || clz.equals(Long.class)
                || clz.equals(int.class) || clz.equals(Integer.class)
                || clz.equals(short.class) || clz.equals(Short.class)
                || clz.equals(byte.class) || clz.equals(Byte.class)
                || clz.equals(float.class) || clz.equals(Float.class)
                || clz.equals(double.class) || clz.equals(Double.class)
        ) {
            indentln("ctx.pushState(getStartNumberValue());");
        } else if (clz.equals(boolean.class) || clz.equals(Boolean.class)) {
            indentln("ctx.pushState(getStartBooleanValue());");
        } else if (Map.class.isAssignableFrom(clz)) {
            outputMapSetter(setter);
        } else if (Collection.class.isAssignableFrom(clz)) {
            outputCollectionSetter(setter);
        } else {
            outputObjectSetter(setter);
        }
    }

    public String contextValue(Class clz, boolean isKey) {
        if (String.class.equals(clz)) {
            return "ContextValue.STRING_VALUE";
        } else if (clz.equals(char.class) || clz.equals(Character.class)) {
            return "ContextValue.CHARACTER_VALUE";
        } else if (clz.equals(OffsetDateTime.class)) {
            if (isKey) {
                throw new RuntimeException("OffsetDateTime is not supported as a Map key type");
            }
            return "ContextValue.OFFSET_DATETIME_VALUE";
        } else if (clz.equals(long.class) || clz.equals(Long.class)) {
            return "ContextValue.LONG_VALUE";
        } else if (clz.equals(int.class) || clz.equals(Integer.class)) {
            return "ContextValue.INT_VALUE";
        } else if (clz.equals(short.class) || clz.equals(Short.class)) {
            return "ContextValue.SHORT_VALUE";
        } else if (clz.equals(byte.class) || clz.equals(Byte.class)) {
            return "ContextValue.BYTE_VALUE";
        } else if (clz.equals(float.class) || clz.equals(Float.class)) {
            return "ContextValue.FLOAT_VALUE";
        } else if (clz.equals(double.class) || clz.equals(Double.class)) {
            return "ContextValue.DOUBLE_VALUE";
        } else if (clz.equals(boolean.class) || clz.equals(Boolean.class)) {
            return "ContextValue.BOOLEAN_VALUE";
        } else if (clz.equals(Object.class)) {
            return isKey ? "ContextValue.STRING_VALUE" : "ContextValue.OBJECT_VALUE";
        } else {
            if (isKey) {
                throw new RuntimeException(clz.getName() + " is not supported as a Map key type");
            }
            return "ContextValue.OBJECT_VALUE";
        }
    }

    public String valueState(Class clz, Type genericType) {
        if (String.class.equals(clz)) {
            return "ObjectParser.PARSER.getStartStringValue()";
        } else if (clz.equals(char.class) || clz.equals(Character.class)) {
            return "ObjectParser.PARSER.getStartStringValue()";
        } else if (clz.equals(OffsetDateTime.class)) {
            return "ObjectParser.PARSER.getStartStringValue()";
        } else if (clz.equals(long.class) || clz.equals(Long.class)) {
            return "ObjectParser.PARSER.getStartIntegerValue()";
        } else if (clz.equals(int.class) || clz.equals(Integer.class)) {
            return "ObjectParser.PARSER.getStartIntegerValue()";
        } else if (clz.equals(short.class) || clz.equals(Short.class)) {
            return "ObjectParser.PARSER.getStartIntegerValue()";
        } else if (clz.equals(byte.class) || clz.equals(Byte.class)) {
            return "ObjectParser.PARSER.getStartIntegerValue()";
        } else if (clz.equals(float.class) || clz.equals(Float.class)) {
            return "ObjectParser.PARSER.getStartNumberValue()";
        } else if (clz.equals(double.class) || clz.equals(Double.class)) {
            return "ObjectParser.PARSER.getStartNumberValue()";
        } else if (clz.equals(boolean.class) || clz.equals(Boolean.class)) {
            return "ObjectParser.PARSER.getStartBooleanValue()";
        } else if (clz.equals(Object.class)) {
            return "GenericParser.PARSER.getStart()";
        } else if (Collection.class.isAssignableFrom(clz)) {
            throw new RuntimeException("Collection not supported yet as a collection or map value type");
        } else if (Map.class.isAssignableFrom(clz)) {
            throw new RuntimeException("Map not supported yet as a collection or map value type");
        } else {
            need(clz);
            return fqn(clz, genericType) + ".PARSER.getStart()";
        }
    }

    public void outputMapSetter(Setter setter) {
        Class keyType = Object.class;
        Class valueType = Object.class;
        Type valueGenericType = null;

        if (setter.genericType != null && setter.genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)setter.genericType;
            keyType = Reflections.getRawType(pt.getActualTypeArguments()[0]);
            valueType = Reflections.getRawType(pt.getActualTypeArguments()[1]);
            valueGenericType = pt.getActualTypeArguments()[1];
        }

        String keyContextValue = contextValue(keyType, true);
        String contextValue = contextValue(valueType, false);
        String valueState = valueState(valueType, valueGenericType);
        String mapType = setter.type.getName();
        if (setter.type.equals(Map.class)) {
            mapType = "java.util.HashMap";
        } else if (setter.type.isInterface()) {
            throw new RuntimeException("Interface not support for property: " + setter.name);
        }
        indentln("ctx.pushTarget(new " + mapType + "());");
        indentln("ctx.pushState(new MapParser(" + keyContextValue + ",");
        indent++;
        indentln(contextValue + ",");
        indentln(valueState + ").getStart()");
        indent--;
        indentln(");");
    }

    public void outputCollectionSetter(Setter setter) {
        Class valueType = Object.class;
        Type valueGenericType = null;

        if (setter.genericType != null && setter.genericType instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType)setter.genericType;
            valueType = Reflections.getRawType(pt.getActualTypeArguments()[0]);
            valueGenericType = pt.getActualTypeArguments()[0];
        }

        String contextValue = contextValue(valueType, false);
        String valueState = valueState(valueType, valueGenericType);
        String collectionType = setter.type.getName();
        if (setter.type.equals(List.class) || setter.type.equals(Collection.class)) {
            collectionType = "java.util.LinkedList";
        } else if (setter.type.isInterface()) {
            throw new RuntimeException("Interface not support for property: " + setter.name);
        }
        indentln("ctx.pushTarget(new " + collectionType + "());");
        indentln("ctx.pushState(new CollectionParser(" + contextValue + ",");
        indent++;
        indentln(valueState + ").getStart()");
        indent--;
        indentln(");");
    }

    public void outputObjectSetter(Setter setter) {
        if (setter.type.equals(Object.class)) {
            indentln("ctx.pushState(GenericParser.PARSER.getStart());");
        } else {
            need(setter.type);
            indentln("ctx.pushState(" + fqn(setter.type, setter.genericType) + ".PARSER.getStart());");
        }
    }

    private void need(Class clz) {
        if (clz.getName().startsWith("java")) {
            throw new RuntimeException("Should not be needed java(x). classes");
        }
        needDeserializerFor.add(clz);
    }

    private void findSetters(Class clz) {
        for (Method m : clz.getMethods()) {
            if (!isSetter(m))
                continue;
            Class paramType = m.getParameterTypes()[0];
            Type paramGenericType = m.getGenericParameterTypes()[0];
            String name;
            if (m.getName().length() > 4) {
                name = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
            } else {
                name = m.getName().substring(3).toLowerCase();
            }
            setters.add(new Setter(name, m, paramType, paramGenericType));
        }
    }
}
