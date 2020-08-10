package io.quarkus.json.generator;

import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.json.deserialize.buffered.IntChar;
import io.quarkus.json.deserialize.buffered.ObjectParser;
import io.quarkus.json.deserialize.buffered.ParserContext;
import io.quarkus.json.deserialize.buffered.SkipParser;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

public class Deserializer {

    // constructor
    public static final String INIT = "<init>";
    // static initializer
    public static final String CLINIT = "<clinit>";

    public static Builder create(Class targetType) {
        return new Builder().type(targetType);
    }

    public static class Builder {
        Class targetType;
        Type targetGenericType;
        ClassOutput output;

        private Builder() {
        }

        public Builder type(Class targetType) {
            this.targetType = targetType;
            return this;
        }

        public Builder generic(Type targetGenericType) {
            this.targetGenericType = targetGenericType;
            return this;
        }

        public Builder output(ClassOutput output) {
            this.output = output;
            return this;
        }

        public void generate() {
            if (targetGenericType == null) targetGenericType = targetType;
            new Deserializer(output, targetType, targetGenericType).generate();
        }
    }

    ClassCreator creator;

    Class targetType;
    Type targetGenericType;
    List<Setter> setters = new LinkedList<>();

    public static String name(Class clz, Type genericType) {
        return clz.getSimpleName() + "__Parser";
    }

    public static String fqn(Class clz, Type genericType) {
        return clz.getName() + "__Parser";
    }

    Deserializer(ClassOutput classOutput, Class targetType, Type targetGenericType) {
        this(classOutput, fqn(targetType, targetGenericType), targetType, targetGenericType);
    }

    Deserializer(ClassOutput classOutput, String className, Class targetType, Type targetGenericType) {
        this.targetType = targetType;
        this.targetGenericType = targetGenericType;
        creator = ClassCreator.builder().classOutput(classOutput)
                .className(className)
                .superClass(ObjectParser.class).build();
    }

    void generate() {
        findSetters(targetType);

        singleton();
        beginObject();
        key();

        creator.close();
    }

    private void key() {
        MethodCreator method = creator.getMethodCreator("key", void.class, ParserContext.class);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));
        AssignableResultHandle c = method.createVariable(int.class);
        chooseField(method, ctx, c, setters, 0);

        ctx.skipToQuote(method);
        method.invokeVirtualMethod(valueSepator(), method.getThis(), ctx.ctx);
        method.invokeVirtualMethod(
                MethodDescriptor.ofMethod(SkipParser.class, "unpushedValue", void.class, ParserContext.class),
                method.readStaticField(FieldDescriptor.of(SkipParser.class, "PARSER", SkipParser.class)),
                        ctx.ctx
                );
        method.returnValue(null);
    }

    private void beginObject() {
        MethodCreator method = creator.getMethodCreator("beginObject", void.class, ParserContext.class);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));
        ResultHandle instance = method.newInstance(MethodDescriptor.ofConstructor(targetType));
        ctx.pushTarget(method, instance);
        method.returnValue(null);
    }

    private void singleton() {
        FieldCreator PARSER = creator.getFieldCreator("PARSER", fqn()).setModifiers(ACC_STATIC | ACC_PUBLIC);


        MethodCreator staticConstructor = creator.getMethodCreator(CLINIT, void.class);
        staticConstructor.setModifiers(ACC_STATIC);
        ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(fqn()));
        staticConstructor.writeStaticField(PARSER.getFieldDescriptor(), instance);
        staticConstructor.returnValue(null);
    }

    private void chooseField(BytecodeCreator scope, _ParserContext ctx, AssignableResultHandle c, List<Setter> setters, int offset) {
        if (setters.size() == 1) {
            BytecodeCreator ifScope = scope.createScope();
            Setter setter = setters.get(0);
            ResultHandle check = ctx.check(ifScope, ifScope.load(setter.name.substring(offset)));
            matchHandler(ctx, setter, ifScope.ifNonZero(check).trueBranch());
            return;
        }
        scope.assign(c, ctx.consume(scope));
        for (int i = 0; i < setters.size(); i++) {
            Setter setter = setters.get(i);
            if (offset >= setter.name.length()) {
                BytecodeCreator ifScope = scope.createScope();
                ResultHandle quote = ifScope.readStaticField(FieldDescriptor.of(IntChar.class, "INT_QUOTE", int.class));
                BranchResult branchResult = ifScope.ifIntegerEqual(c, quote);
                BytecodeCreator trueBranch = branchResult.trueBranch();
                matchHandler(ctx, setter, trueBranch);
                scope = branchResult.falseBranch();
                continue;
            }
            char ch = setter.name.charAt(offset);
            List<Setter> sameChars = new ArrayList<>();
            sameChars.add(setter);
            BytecodeCreator ifScope = scope.createScope();
            BranchResult branchResult = ifScope.ifIntegerEqual(c, ifScope.load(ch));
            for (i = i + 1; i < setters.size(); i++) {
                Setter next = setters.get(i);
                if (offset < next.name.length() && next.name.charAt(offset) == ch) {
                    sameChars.add(next);
                } else {
                    i--;
                    break;
                }
            }
            chooseField(branchResult.trueBranch(), ctx, c, sameChars, offset + 1);
            scope = branchResult.falseBranch();
        }

    }

    private void matchHandler(_ParserContext ctx, Setter setter, BytecodeCreator scope) {
        MethodDescriptor valueSeparator = valueSepator();
        scope.invokeVirtualMethod(valueSeparator, scope.getThis(), ctx.ctx);
        MethodDescriptor startIntegerValue = MethodDescriptor.ofMethod(fqn(), "startIntegerValue", void.class.getName(), ParserContext.class.getName());
        scope.invokeVirtualMethod(startIntegerValue, scope.getThis(), ctx.ctx);
        AssignableResultHandle target = scope.createVariable(targetType);
        scope.assign(target, ctx.target(scope));
        AssignableResultHandle value = scope.createVariable(setter.type);
        scope.assign(value, ctx.popIntToken(scope));
        MethodDescriptor set = MethodDescriptor.ofMethod(setter.method);
        scope.invokeVirtualMethod(set, target, value);
        scope.returnValue(null);
    }

    private MethodDescriptor valueSepator() {
        return MethodDescriptor.ofMethod(fqn(), "valueSeparator", void.class.getName(), ParserContext.class.getName());
    }

    private String fqn() {
        return fqn(targetType, targetGenericType);
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
        Collections.sort(setters, (setter, t1) -> setter.name.compareTo(t1.name));
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


    static class _ParserContext {
        ResultHandle ctx;

        public _ParserContext(ResultHandle ctx) {
            this.ctx = ctx;
        }

        public ResultHandle consume(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "consume", int.class), ctx);
        }

        public void clearToken(BytecodeCreator scope) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "clearToken", void.class), ctx);
        }

        public ResultHandle check(BytecodeCreator scope, ResultHandle str) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "check", boolean.class, String.class), ctx, str);
        }


        public ResultHandle popToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popToken", boolean.class), ctx);
        }


        public ResultHandle popBooleanToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popBooleanToken", boolean.class), ctx);
        }

        public ResultHandle popIntToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popIntToken", int.class), ctx);
        }

        public ResultHandle popLongToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popLongToken", long.class), ctx);
        }

        public ResultHandle target(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "target", Object.class), ctx);
        }

        public void pushTarget(BytecodeCreator scope, ResultHandle obj) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "pushTarget", void.class, Object.class), ctx, obj);
        }

        public ResultHandle popTarget(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popTarget", Object.class), ctx);
        }

        public ResultHandle skipToQuote(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "skipToQuote", int.class), ctx);
        }


    }


}
