package io.quarkus.json.generator;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

public class DeserializerGenerator {
    private PrintWriter writer;
    int indent;

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
        Class type;
        Type genericType;

        public Setter(String name, Class type, Type genericType) {
            this.name = name;
            this.type = type;
            this.genericType = genericType;
        }
    }

    public static String name(Class clz) {
        return clz.getSimpleName() + "__Parser";
    }

    public static String fqn(Class clz) {
        return clz.getName() + "__Parser";
    }

    List<Setter> setters = new LinkedList<>();

    public void generate(Class clz, Type genericType) {
        String parser = name(clz);
        findSetters(clz);

        indentln("package " + clz.getPackage().getName());
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
        indentln("ctx.pushTarget(new " + clz.getName() + "());");
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
                }
                else {
                    print(" else ");
                }
                println("if (key.equals(\"" + setter.name + "\")) {");
                indent++;
                indent--;
                indent("}");
            }

        }
        indentln("return true;");
        indent--;
        indentln("}");






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
            setters.add(new Setter(name, paramType, paramGenericType));
        }
    }
}
