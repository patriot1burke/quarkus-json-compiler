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
import io.quarkus.json.deserializer.nio.CollectionParser;
import io.quarkus.json.deserializer.nio.ContextValue;
import io.quarkus.json.deserializer.nio.MapParser;
import io.quarkus.json.deserializer.nio.ObjectParser;
import io.quarkus.json.deserializer.nio.ParserContext;
import io.quarkus.json.deserializer.nio.ParserState;
import io.quarkus.json.generator.Types;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    Map<Class, Type> requiredParesrs = new HashMap<>();

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
            collectionField(staticConstructor, setter);
            MethodCreator method = propertyEndMethod(setter);
            propertyEndFunction(setter, staticConstructor, method);
        }
        staticConstructor.returnValue(null);
    }

    private void collectionField(MethodCreator staticConstructor, Setter setter) {
        if (setter.genericType instanceof ParameterizedType) {
            if (Map.class.isAssignableFrom(setter.type)) {
                ParameterizedType pt = (ParameterizedType) setter.genericType;
                Type keyType = pt.getActualTypeArguments()[0];
                Class keyClass = Types.getRawType(keyType);
                ResultHandle keyContextValue = contextValue(keyClass, keyType, staticConstructor);
                Type valueType = pt.getActualTypeArguments()[1];
                Class valueClass = Types.getRawType(valueType);
                ResultHandle valueContextValue = contextValue(valueClass, valueType, staticConstructor);
                ResultHandle valueState = valueState(valueClass, valueType, staticConstructor);
                ResultHandle continueValueState = continueValueState(valueClass, valueType, staticConstructor);
                FieldCreator mapParser = creator.getFieldCreator(setter.property, MapParser.class).setModifiers(ACC_STATIC | ACC_PRIVATE | ACC_FINAL);
                ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(MapParser.class, ContextValue.class, ContextValue.class, ParserState.class, ParserState.class),
                        keyContextValue, valueContextValue, valueState, continueValueState);
                staticConstructor.writeStaticField(mapParser.getFieldDescriptor(), instance);
            } else if (List.class.isAssignableFrom(setter.type)
                        || Set.class.isAssignableFrom(setter.type)) {
                ParameterizedType pt = (ParameterizedType) setter.genericType;
                Type valueType = pt.getActualTypeArguments()[0];
                Class valueClass = Types.getRawType(valueType);
                ResultHandle valueContextValue = contextValue(valueClass, valueType, staticConstructor);
                ResultHandle valueState = valueState(valueClass, valueType, staticConstructor);
                FieldCreator collectionParser = creator.getFieldCreator(setter.property, CollectionParser.class).setModifiers(ACC_STATIC | ACC_PRIVATE | ACC_FINAL);
                ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(CollectionParser.class, ContextValue.class, ParserState.class),
                        valueContextValue, valueState);
                staticConstructor.writeStaticField(collectionParser.getFieldDescriptor(), instance);

            } else {
                // ignore we don't need a special parser
            }
        }

    }

    private ResultHandle contextValue(Class type, Type genericType, BytecodeCreator scope) {
        if (type.equals(String.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "STRING_VALUE", ContextValue.class));
        } else if (type.equals(short.class) || type.equals(Short.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "SHORT_VALUE", ContextValue.class));
        } else if (type.equals(byte.class) || type.equals(Byte.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "BYTE_VALUE", ContextValue.class));
        } else if (type.equals(int.class) || type.equals(Integer.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "INT_VALUE", ContextValue.class));
        } else if (type.equals(long.class) || type.equals(Long.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "LONG_VALUE", ContextValue.class));
        } else if (type.equals(float.class) || type.equals(Float.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "FLOAT_VALUE", ContextValue.class));
        } else if (type.equals(double.class) || type.equals(Double.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "DOUBLE_VALUE", ContextValue.class));
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "BOOLEAN_VALUE", ContextValue.class));
        } else if (type.equals(char.class) || type.equals(Character.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "CHAR_VALUE", ContextValue.class));
        } else if (type.equals(OffsetDateTime.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "OFFSET_DATETIME_VALUE", ContextValue.class));
        } else if (type.equals(BigDecimal.class)) {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "BIGDECIMAL_VALUE", ContextValue.class));
        } else {
            return scope.readStaticField(FieldDescriptor.of(ContextValue.class, "OBJECT_VALUE", ContextValue.class));
        }
    }

    private ResultHandle valueState(Class type, Type genericType, BytecodeCreator scope) {
        if (type.equals(String.class)
                || type.equals(char.class) || type.equals(Character.class)
                || type.equals(OffsetDateTime.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "startStringValue", ParserState.class), PARSER);
        } else if (type.equals(short.class) || type.equals(Short.class)
                || type.equals(byte.class) || type.equals(Byte.class)
                || type.equals(int.class) || type.equals(Integer.class)
                || type.equals(long.class) || type.equals(Long.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "startIntegerValue", ParserState.class), PARSER);
        } else if (type.equals(float.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(BigDecimal.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "startNumberValue", ParserState.class), PARSER);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "startBooleanValue", ParserState.class), PARSER);
        } else {
            // todo handle nested collections and maps
            FieldDescriptor parserField = FieldDescriptor.of(fqn(type, genericType), "PARSER", fqn(type, genericType));
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "start", ParserState.class), PARSER);
        }
    }

    private ResultHandle continueValueState(Class type, Type genericType, BytecodeCreator scope) {
        if (type.equals(String.class)
                || type.equals(char.class) || type.equals(Character.class)
                || type.equals(OffsetDateTime.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartStringValue", ParserState.class), PARSER);
        } else if (type.equals(short.class) || type.equals(Short.class)
                || type.equals(byte.class) || type.equals(Byte.class)
                || type.equals(int.class) || type.equals(Integer.class)
                || type.equals(long.class) || type.equals(Long.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartIntegerValue", ParserState.class), PARSER);
        } else if (type.equals(float.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(BigDecimal.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartNumberValue", ParserState.class), PARSER);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartBooleanValue", ParserState.class), PARSER);
        } else {
            // todo handle nested collections and maps
            FieldDescriptor parserField = FieldDescriptor.of(fqn(type, genericType), "PARSER", fqn(type, genericType));
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStart", ParserState.class), PARSER);
        }
    }

    private void propertyEndFunction(Setter setter, MethodCreator staticConstructor, MethodCreator method) {
        String endName = endProperty(setter);
        FieldCreator endField = creator.getFieldCreator(endName, ParserState.class).setModifiers(ACC_STATIC | ACC_PRIVATE);
        FunctionCreator endFunction = staticConstructor.createFunction(ParserState.class);
        BytecodeCreator ebc = endFunction.getBytecode();
        _ParserContext ctx = new _ParserContext(ebc.getMethodParam(0));
        ctx.popState(ebc);
        ebc.invokeStaticMethod(method.getMethodDescriptor(), ctx.ctx);
        ebc.returnValue(ebc.load(true));
        staticConstructor.writeStaticField(endField.getFieldDescriptor(), endFunction.getInstance());
    }

    private MethodCreator propertyEndMethod(Setter setter) {
        String endName = endProperty(setter);
        MethodCreator method = creator.getMethodCreator(endName, void.class, ParserContext.class);
        method.setModifiers(ACC_STATIC | ACC_FINAL | ACC_PUBLIC);
        _ParserContext ctx = new _ParserContext(method.getMethodParam(0));
        ResultHandle popSetter = popSetterValue(ctx, setter, method);
        AssignableResultHandle target = method.createVariable(targetType);
        method.assign(target, ctx.target(method));
        MethodDescriptor set = MethodDescriptor.ofMethod(setter.method);
        method.invokeVirtualMethod(set, target, popSetter);
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
        collectionTarget(ctx, scope, setter);
        BytecodeCreator ifScope = scope.createScope();
        MethodDescriptor valueSeparator = valueSeparator();
        ResultHandle passed = ifScope.invokeVirtualMethod(valueSeparator, scope.getThis(), ctx.ctx);
        ifScope = ifScope.ifZero(passed).trueBranch();
        ctx.pushState(ifScope,
                continueState(setter, ifScope),
                stateIndex);
        ctx.pushState(ifScope,
                ifScope.readStaticField(FieldDescriptor.of(fqn(), endProperty(setter), ParserState.class)), stateIndex);
        ifScope.returnValue(ifScope.load(false));

        ifScope = scope.createScope();
        passed = callStartState(ctx, setter, ifScope);
        ifScope = ifScope.ifZero(passed).trueBranch();
        ctx.pushState(ifScope,
                ifScope.readStaticField(FieldDescriptor.of(fqn(), endProperty(setter), ParserState.class)),
                        stateIndex);
        ifScope.returnValue(ifScope.load(false));

        scope.invokeStaticMethod(MethodDescriptor.ofMethod(fqn(), endProperty(setter), void.class, ParserContext.class), ctx.ctx);
        scope.returnValue(scope.load(true));
    }

    private void collectionTarget(_ParserContext ctx,BytecodeCreator scope, Setter setter) {
        if (setter.genericType instanceof ParameterizedType) {
            Class target = null;
            if (Map.class.equals(setter.type) || (setter.type.isInterface() && Map.class.isAssignableFrom(setter.type))) {
                target = HashMap.class;
            } else if (Map.class.isAssignableFrom(setter.type) && !setter.type.isInterface()) {
                target = setter.type;
            } else if (List.class.equals(setter.type) || (setter.type.isInterface() && List.class.isAssignableFrom(setter.type))) {
                target = LinkedList.class;
            } else if (List.class.isAssignableFrom(setter.type) && !setter.type.isInterface()) {
                target = setter.type;
            } else if (Set.class.equals(setter.type) || (setter.type.isInterface() && Set.class.isAssignableFrom(setter.type))) {
                target = HashSet.class;
            } else if (Set.class.isAssignableFrom(setter.type) && !setter.type.isInterface()) {
                target = setter.type;
            }
            if (target != null) {
                ResultHandle instance = scope.newInstance(MethodDescriptor.ofConstructor(target));
                ctx.pushTarget(scope, instance);
            }
        }
    }

    private ResultHandle continueState(Setter setter, BytecodeCreator scope) {
        Class type = setter.type;
        Type genericType = setter.genericType;
        if (type.equals(String.class)
                || type.equals(char.class) || type.equals(Character.class)
                || type.equals(OffsetDateTime.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartStringValue", ParserState.class), PARSER);
        } else if (type.equals(short.class) || type.equals(Short.class)
                || type.equals(byte.class) || type.equals(Byte.class)
                || type.equals(int.class) || type.equals(Integer.class)
                || type.equals(long.class) || type.equals(Long.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartIntegerValue", ParserState.class), PARSER);
        } else if (type.equals(float.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(BigDecimal.class)
        ) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartNumberValue", ParserState.class), PARSER);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            FieldDescriptor parserField = FieldDescriptor.of(ObjectParser.class, "PARSER", ObjectParser.class);
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStartBooleanValue", ParserState.class), PARSER);
        } else if (List.class.isAssignableFrom(setter.type)
                || Set.class.isAssignableFrom(setter.type)) {
            if (setter.genericType instanceof ParameterizedType) {
                // continue is on static field
                FieldDescriptor mapFieldDesc = FieldDescriptor.of(fqn(type, genericType), setter.property, CollectionParser.class);
                ResultHandle mapField = scope.readStaticField(mapFieldDesc);
                return scope.readInstanceField(FieldDescriptor.of(CollectionParser.class, "continueStart", ParserState.class), mapField);
            } else {
                // generic
                throw new RuntimeException("generic not supported yet");
            }
        } else if (Map.class.isAssignableFrom(setter.type)) {
            if (setter.genericType instanceof ParameterizedType) {
                FieldDescriptor mapFieldDesc = FieldDescriptor.of(fqn(type, genericType), setter.property, MapParser.class);
                ResultHandle mapField = scope.readStaticField(mapFieldDesc);
                return scope.readInstanceField(FieldDescriptor.of(MapParser.class, "continueStart", ParserState.class), mapField);
            } else {
                // generic
                throw new RuntimeException("generic not supported yet");
            }
        } else if (setter.type.equals(Object.class)) {
            // pure generic
            throw new RuntimeException("generic not supported yet");
        } else {
            // todo handle nested collections and maps
            FieldDescriptor parserField = FieldDescriptor.of(fqn(type, genericType), "PARSER", fqn(type, genericType));
            ResultHandle PARSER = scope.readStaticField(parserField);
            return scope.readInstanceField(FieldDescriptor.of(ObjectParser.class, "continueStart", ParserState.class), PARSER);
        }
    }

    private ResultHandle callStartState(_ParserContext ctx, Setter setter, BytecodeCreator scope) {
        Class type = setter.type;
        Type genericType = setter.genericType;
        if (type.equals(String.class)
                || type.equals(char.class) || type.equals(Character.class)
                || type.equals(OffsetDateTime.class)
        ) {
            MethodDescriptor descriptor = MethodDescriptor.ofMethod(fqn(), "startStringValue", boolean.class.getName(), ParserContext.class.getName());
            return scope.invokeVirtualMethod(descriptor, scope.getThis(), ctx.ctx);
        } else if (type.equals(short.class) || type.equals(Short.class)
                || type.equals(byte.class) || type.equals(Byte.class)
                || type.equals(int.class) || type.equals(Integer.class)
                || type.equals(long.class) || type.equals(Long.class)
        ) {
            MethodDescriptor descriptor = MethodDescriptor.ofMethod(fqn(), "startIntegerValue", boolean.class.getName(), ParserContext.class.getName());
            return scope.invokeVirtualMethod(descriptor, scope.getThis(), ctx.ctx);
        } else if (type.equals(float.class) || type.equals(Float.class)
                || type.equals(double.class) || type.equals(Double.class)
                || type.equals(BigDecimal.class)
        ) {
            MethodDescriptor descriptor = MethodDescriptor.ofMethod(fqn(), "startNumberValue", boolean.class.getName(), ParserContext.class.getName());
            return scope.invokeVirtualMethod(descriptor, scope.getThis(), ctx.ctx);
        } else if (type.equals(boolean.class) || type.equals(Boolean.class)) {
            MethodDescriptor descriptor = MethodDescriptor.ofMethod(fqn(), "startBooleanValue", boolean.class.getName(), ParserContext.class.getName());
            return scope.invokeVirtualMethod(descriptor, scope.getThis(), ctx.ctx);
        } else if (List.class.isAssignableFrom(setter.type)
                || Set.class.isAssignableFrom(setter.type)) {
            if (setter.genericType instanceof ParameterizedType) {
                // invoke static field for property
                MethodDescriptor descriptor = MethodDescriptor.ofMethod(CollectionParser.class, "start", boolean.class, ParserContext.class);
                return scope.invokeVirtualMethod(descriptor,
                        scope.readStaticField(FieldDescriptor.of(fqn(), setter.property, CollectionParser.class)),
                        ctx.ctx);
            } else {
                // generic
                throw new RuntimeException("generic not supported yet");
            }
        } else if (Map.class.isAssignableFrom(setter.type)) {
            if (setter.genericType instanceof ParameterizedType) {
                // invoke static field for property
                MethodDescriptor descriptor = MethodDescriptor.ofMethod(MapParser.class, "start", boolean.class, ParserContext.class);
                return scope.invokeVirtualMethod(descriptor,
                        scope.readStaticField(FieldDescriptor.of(fqn(), setter.property, MapParser.class)),
                        ctx.ctx);
            } else {
                // generic
                throw new RuntimeException("generic not supported yet");
            }
        } else if (setter.type.equals(Object.class)) {
            // pure generic
            throw new RuntimeException("generic not supported yet");
        } else {
            FieldDescriptor parserField = FieldDescriptor.of(fqn(type, genericType), "PARSER", fqn(type, genericType));
            ResultHandle PARSER = scope.readStaticField(parserField);
            MethodDescriptor descriptor = MethodDescriptor.ofMethod(fqn(type, genericType), "start", boolean.class.getName(), ParserContext.class.getName());
            return scope.invokeVirtualMethod(descriptor, PARSER, ctx.ctx);
        }
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
