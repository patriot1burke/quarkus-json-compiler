package io.quarkus.json.generator.nio;

import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.BranchResult;
import io.quarkus.gizmo.BytecodeCreator;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.FunctionCreator;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.json.deserializer.nio.BaseParser;
import io.quarkus.json.deserializer.nio.ObjectParser;
import io.quarkus.json.deserializer.nio.ParserContext;
import io.quarkus.json.deserializer.nio.ParserState;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
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

        staticInitializer();
        beginObject();
        key();

        creator.close();
    }

    private void beginObject() {
        MethodCreator method = creator.getMethodCreator("beginObject", void.class, ParserContext.class);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));
        ResultHandle instance = method.newInstance(MethodDescriptor.ofConstructor(targetType));
        ctx.pushTarget(method, instance);
        method.returnValue(null);
    }

    private void staticInitializer() {
        FieldCreator PARSER = creator.getFieldCreator("PARSER", fqn()).setModifiers(ACC_STATIC | ACC_PUBLIC);


        MethodCreator staticConstructor = creator.getMethodCreator(CLINIT, void.class);
        staticConstructor.setModifiers(ACC_STATIC);
        ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(fqn()));
        staticConstructor.writeStaticField(PARSER.getFieldDescriptor(), instance);

        for (Setter setter : setters) {
            String endName = endProperty(setter);
            FieldCreator endField = creator.getFieldCreator(endName, ParserState.class).setModifiers(ACC_STATIC | ACC_PRIVATE);
            MethodCreator method = propertyEndMethod(setter, endName);
            propertyEndFunction(staticConstructor, endField, method);
        }
        staticConstructor.returnValue(null);
    }

    private void propertyEndFunction(MethodCreator staticConstructor, FieldCreator endField, MethodCreator method) {
        FunctionCreator endFunction = staticConstructor.createFunction(ParserState.class);
        BytecodeCreator ebc = endFunction.getBytecode();
        _ParserContext ctx = new _ParserContext(ebc.getMethodParam(0));
        ctx.popState(ebc);
        ebc.invokeStaticMethod(method.getMethodDescriptor(), ctx.ctx);
        ebc.returnValue(ebc.load(true));
        staticConstructor.writeStaticField(endField.getFieldDescriptor(), endFunction.getInstance());
    }

    private MethodCreator propertyEndMethod(Setter setter, String endName) {
        MethodCreator method = creator.getMethodCreator(endName, void.class, ParserContext.class);
        method.setModifiers(ACC_STATIC | ACC_FINAL | ACC_PUBLIC);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));
        AssignableResultHandle target = method.createVariable(targetType);
        method.assign(target, ctx.target(method));
        MethodDescriptor set = MethodDescriptor.ofMethod(setter.method);
        method.invokeVirtualMethod(set, target, popSetterValue(ctx, setter, method));
        method.returnValue(null);
        return method;
    }

    private ResultHandle popSetterValue(_ParserContext ctx, Setter setter, BytecodeCreator scope) {
        if (setter.type.equals(String.class)) {
            return ctx.popToken(scope);
        } else if (setter.type.equals(short.class) || setter.type.equals(Short.class)) {
            return ctx.popShortToken(scope);
        } else if (setter.type.equals(byte.class) || setter.type.equals(Byte.class)) {
            return ctx.popByteToken(scope);
        } else if (setter.type.equals(int.class) || setter.type.equals(Integer.class)) {
            return ctx.popIntToken(scope);
        } else if (setter.type.equals(long.class) || setter.type.equals(Long.class)) {
            return ctx.popLongToken(scope);
        } else if (setter.type.equals(float.class) || setter.type.equals(Float.class)) {
            return ctx.popFloatToken(scope);
        } else if (setter.type.equals(double.class) || setter.type.equals(Double.class)) {
            return ctx.popDoubleToken(scope);
        } else if (setter.type.equals(boolean.class) || setter.type.equals(Boolean.class)) {
            return ctx.popBooleanToken(scope);
        } else {
            return ctx.popTarget(scope);
        }
    }

    private String endProperty(Setter setter) {
        return setter.property + "End";
    }

    private void key() {
        MethodCreator method = creator.getMethodCreator("key", boolean.class, ParserContext.class);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));

        BytecodeCreator scope = method.createScope();
        BytecodeCreator ifTrue = scope.ifIntegerEqual(ctx.skipToQuote(scope), scope.load((int)0)).trueBranch();
        ctx.pushState(ifTrue, ifTrue.readInstanceField(FieldDescriptor.of(fqn(), "continueKey", ParserState.class), ifTrue.getThis()));
        ifTrue.returnValue(ifTrue.load(false));
        ctx.endToken(method);
        ResultHandle stateIndex = ctx.stateIndex(method);

        chooseField(method, ctx, stateIndex, setters, 0);

        ResultHandle result = method.invokeVirtualMethod(MethodDescriptor.ofMethod(BaseParser.class, "skipValue", boolean.class, ParserContext.class),
                method.readStaticField(FieldDescriptor.of(BaseParser.class, "PARSER", BaseParser.class)), ctx.ctx);
        method.returnValue(result);
    }

    private void chooseField(BytecodeCreator scope, _ParserContext ctx, ResultHandle stateIndex, List<Setter> setters, int offset) {
        if (setters.size() == 1) {
            Setter setter = setters.get(0);
            compareToken(scope, ctx, stateIndex, offset, setter);
            return;
        }
        ResultHandle c = ctx.tokenCharAt(scope, offset);
        for (int i = 0; i < setters.size(); i++) {
            Setter setter = setters.get(i);
            if (offset >= setter.name.length()) {
                compareToken(scope, ctx, stateIndex, offset, setter);
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
            chooseField(branchResult.trueBranch(), ctx, stateIndex, sameChars, offset + 1);
            scope = branchResult.falseBranch();
        }

    }

    private void compareToken(BytecodeCreator scope, _ParserContext ctx, ResultHandle stateIndex, int offset, Setter setter) {
        BytecodeCreator ifScope = scope.createScope();
        ResultHandle check = ctx.compareToken(ifScope, ifScope.load(offset), ifScope.load(setter.name.substring(offset)));
        matchHandler(ctx, stateIndex, setter, ifScope.ifNonZero(check).trueBranch());
    }

    private void matchHandler(_ParserContext ctx, ResultHandle stateIndex, Setter setter, BytecodeCreator scope) {
        BytecodeCreator ifScope = scope.createScope();
        MethodDescriptor valueSeparator = valueSeparator();
        ResultHandle passed = ifScope.invokeVirtualMethod(valueSeparator, scope.getThis(), ctx.ctx);
        ifScope = ifScope.ifZero(passed).trueBranch();
        ctx.pushState(ifScope,
                ifScope.readInstanceField(FieldDescriptor.of(fqn(), "continueStartIntegerValue", ParserState.class), scope.getThis()),
                stateIndex);
        ctx.pushState(ifScope,
                ifScope.readStaticField(FieldDescriptor.of(fqn(), endProperty(setter), ParserState.class)), stateIndex);
        ifScope.returnValue(ifScope.load(false));

        ifScope = scope.createScope();
        MethodDescriptor startIntegerValue = MethodDescriptor.ofMethod(fqn(), "startIntegerValue", boolean.class.getName(), ParserContext.class.getName());
        passed = ifScope.invokeVirtualMethod(startIntegerValue, scope.getThis(), ctx.ctx);
        ifScope = ifScope.ifZero(passed).trueBranch();
        ctx.pushState(ifScope,
                ifScope.readStaticField(FieldDescriptor.of(fqn(), endProperty(setter), ParserState.class)),
                        stateIndex);
        ifScope.returnValue(ifScope.load(false));

        scope.invokeStaticMethod(MethodDescriptor.ofMethod(fqn(), endProperty(setter), void.class, ParserContext.class), ctx.ctx);
        scope.returnValue(scope.load(true));
    }

    private MethodDescriptor valueSeparator() {
        return MethodDescriptor.ofMethod(fqn(), "valueSeparator", boolean.class.getName(), ParserContext.class.getName());
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
        String name; // json key
        String property; // this is method name without set/get
        Method method;
        Class type;
        Type genericType;

        public Setter(String name, Method method, Class type, Type genericType) {
            this.name = name;
            this.property = name;
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

        public void popState(BytecodeCreator scope) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popState", void.class), ctx);
        }

        public void endToken(BytecodeCreator scope) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "endToken", void.class), ctx);
        }

        public ResultHandle compareToken(BytecodeCreator scope, ResultHandle index, ResultHandle str) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "compareToken", boolean.class, int.class, String.class), ctx, index, str);
        }

        public ResultHandle tokenCharAt(BytecodeCreator scope, int index) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "tokenCharAt", int.class, int.class), ctx, scope.load(index));
        }


        public ResultHandle popToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popToken", String.class), ctx);
        }


        public ResultHandle popBooleanToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popBooleanToken", boolean.class), ctx);
        }

        public ResultHandle popIntToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popIntToken", int.class), ctx);
        }

        public ResultHandle popShortToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popShortToken", short.class), ctx);
        }

        public ResultHandle popByteToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popByteToken", byte.class), ctx);
        }

        public ResultHandle popLongToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popLongToken", long.class), ctx);
        }

        public ResultHandle popFloatToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popFloatToken", float.class), ctx);
        }

        public ResultHandle popDoubleToken(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popDoubleToken", double.class), ctx);
        }

        public ResultHandle target(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "target", Object.class), ctx);
        }

        public void pushTarget(BytecodeCreator scope, ResultHandle obj) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "pushTarget", void.class, Object.class), ctx, obj);
        }

        public void pushState(BytecodeCreator scope, ResultHandle func, ResultHandle index) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "pushState", void.class, ParserState.class, int.class), ctx, func, index);
        }

        public void pushState(BytecodeCreator scope, ResultHandle func) {
            scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "pushState", void.class, ParserState.class), ctx, func);
        }

        public ResultHandle popTarget(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "popTarget", Object.class), ctx);
        }

        public ResultHandle stateIndex(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "stateIndex", int.class), ctx);
        }

        public ResultHandle skipToQuote(BytecodeCreator scope) {
            return scope.invokeVirtualMethod(MethodDescriptor.ofMethod(ParserContext.class, "skipToQuote", int.class), ctx);
        }


    }


}
