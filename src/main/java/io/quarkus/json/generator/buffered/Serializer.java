package io.quarkus.json.generator.buffered;

import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.json.deserializer.nio.ContextValue;
import io.quarkus.json.deserializer.nio.ListParser;
import io.quarkus.json.deserializer.nio.MapParser;
import io.quarkus.json.deserializer.nio.ParserState;
import io.quarkus.json.deserializer.nio.SetParser;
import io.quarkus.json.generator.Types;
import io.quarkus.json.generator.nio.Deserializer;
import io.quarkus.json.serializer.bio.CollectionWriter;
import io.quarkus.json.serializer.bio.JsonWriter;
import io.quarkus.json.serializer.bio.MapWriter;
import io.quarkus.json.serializer.bio.ObjectWriter;
import org.jboss.logging.annotations.Param;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.objectweb.asm.Opcodes.*;
import static org.objectweb.asm.Opcodes.ACC_FINAL;

public class Serializer {

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
            new Serializer(output, targetType, targetGenericType).generate();
        }
    }

    ClassCreator creator;

    Class targetType;
    Type targetGenericType;
    List<Getter> getters = new LinkedList<>();
    HashMap<Class, Type> needed = new HashMap<>();

    public static String name(Class clz, Type genericType) {
        return clz.getSimpleName() + "__Serializer";
    }

    public static String fqn(Class clz, Type genericType) {
        return clz.getName() + "__Serializer";
    }

    Serializer(ClassOutput classOutput, Class targetType, Type targetGenericType) {
        this(classOutput, fqn(targetType, targetGenericType), targetType, targetGenericType);
    }

    Serializer(ClassOutput classOutput, String className, Class targetType, Type targetGenericType) {
        this.targetType = targetType;
        this.targetGenericType = targetGenericType;
        creator = ClassCreator.builder().classOutput(classOutput)
                .className(className)
                .interfaces(ObjectWriter.class).build();
    }

    void generate() {
        findGetters(targetType);
        singleton();
        writeMethod();
        creator.close();
    }

    private void singleton() {
        FieldCreator SERIALIZER = creator.getFieldCreator("SERIALIZER", fqn()).setModifiers(ACC_STATIC | ACC_PUBLIC);


        MethodCreator staticConstructor = creator.getMethodCreator(CLINIT, void.class);
        staticConstructor.setModifiers(ACC_STATIC);
        ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(fqn()));
        staticConstructor.writeStaticField(SERIALIZER.getFieldDescriptor(), instance);
        for (Getter getter : getters) {
            collectionField(staticConstructor, getter);
        }

        staticConstructor.returnValue(null);
    }

    private void collectionField(MethodCreator staticConstructor, Getter getter) {
        Type genericType = getter.genericType;
        Class type = getter.type;
        String property = getter.property;
        collectionField(staticConstructor, type, genericType, property);

    }
    private ResultHandle getNestedValueWriter(MethodCreator staticConstructor, Class type, Type genericType, String property) {
        if (!hasNestedWriter(type, genericType)) return null;
        if (isUserObject(type)) {
            return staticConstructor.readStaticField(FieldDescriptor.of(fqn(type, genericType), "SERIALIZER", fqn(type, genericType)));
        }
        collectionField(staticConstructor, type, genericType, property);
        return staticConstructor.readStaticField(FieldDescriptor.of(fqn(), property, ObjectWriter.class));
    }

    private void collectionField(MethodCreator staticConstructor, Class type, Type genericType, String property) {
        if (genericType instanceof ParameterizedType) {
            if (Map.class.isAssignableFrom(type)) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Type valueType = pt.getActualTypeArguments()[1];
                Class valueClass = Types.getRawType(valueType);
                ResultHandle nested = getNestedValueWriter(staticConstructor, valueClass, valueType, property + "_n");
                if (nested == null) return;

                FieldCreator mapWriter = creator.getFieldCreator(property, ObjectWriter.class).setModifiers(ACC_STATIC | ACC_PRIVATE | ACC_FINAL);
                ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(MapWriter.class, ObjectWriter.class),
                        nested);
                staticConstructor.writeStaticField(mapWriter.getFieldDescriptor(), instance);
            } else if (List.class.isAssignableFrom(type) || Set.class.isAssignableFrom(type)) {
                ParameterizedType pt = (ParameterizedType) genericType;
                Type valueType = pt.getActualTypeArguments()[0];
                Class valueClass = Types.getRawType(valueType);
                ResultHandle nested = getNestedValueWriter(staticConstructor, valueClass, valueType, property + "_n");
                if (nested == null) return;

                FieldCreator mapWriter = creator.getFieldCreator(property, ObjectWriter.class).setModifiers(ACC_STATIC | ACC_PRIVATE | ACC_FINAL);
                ResultHandle instance = staticConstructor.newInstance(MethodDescriptor.ofConstructor(CollectionWriter.class, ObjectWriter.class),
                        nested);
                staticConstructor.writeStaticField(mapWriter.getFieldDescriptor(), instance);
            } else {
                // ignore we don't need a special parser
            }
        }
    }


    private void writeMethod() {
        MethodCreator method = creator.getMethodCreator("write", void.class, JsonWriter.class, Object.class);
        ResultHandle jsonWriter = method.getMethodParam(0);
        AssignableResultHandle target = method.createVariable(targetType);
        method.assign(target, method.getMethodParam(1));
        AssignableResultHandle comma = method.createVariable(boolean.class);
        method.assign(comma, method.load(false));
        boolean forceComma = false;
        // todo support an interface as type
        for (Getter getter : getters) {
            if (getter.type.equals(int.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, int.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Integer.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Integer.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(short.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, short.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Short.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Short.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(long.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, long.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Long.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Long.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(byte.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, byte.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Byte.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Byte.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(boolean.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, boolean.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Boolean.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Boolean.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(float.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, float.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Float.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Float.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(double.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, double.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Double.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Double.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(char.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, char.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) {
                    method.assign(comma, method.load(true));
                    forceComma = true;
                }
            } else if (getter.type.equals(Character.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Character.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (getter.type.equals(String.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, String.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else if (Map.class.isAssignableFrom(getter.type)) {
                if (hasCollectionWriter(getter)) {
                    ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeObjectProperty", boolean.class, String.class, Object.class, ObjectWriter.class, boolean.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            method.readStaticField(FieldDescriptor.of(fqn(), getter.property, ObjectWriter.class)),
                            comma
                    );
                    if (!forceComma) method.assign(comma, result);
                } else {
                    ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Map.class, boolean.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            comma);
                    if (!forceComma) method.assign(comma, result);
                }
            } else if (Collection.class.isAssignableFrom(getter.type)) {
                if (hasCollectionWriter(getter)) {
                    ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeObjectProperty", boolean.class, String.class, Object.class, ObjectWriter.class, boolean.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            method.readStaticField(FieldDescriptor.of(fqn(), getter.property, ObjectWriter.class)),
                            comma
                    );
                    if (!forceComma) method.assign(comma, result);
                } else {
                    ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", boolean.class, String.class, Collection.class, boolean.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            comma);
                    if (!forceComma) method.assign(comma, result);
                }
            } else if (getter.type.equals(Object.class)) {
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeObjectProperty", boolean.class, String.class, Object.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        comma);
                if (!forceComma) method.assign(comma, result);
            } else {
                needed.put(getter.type, getter.genericType);
                ResultHandle result = method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeObjectProperty", boolean.class, String.class, Object.class, ObjectWriter.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        method.readStaticField(FieldDescriptor.of(fqn(getter.type, getter.genericType), "SERIALIZER", fqn(getter.type, getter.genericType))),
                        comma
                );
                if (!forceComma) method.assign(comma, result);
            }
        }
        method.returnValue(null);
    }

    private boolean isUserObject(Class type) {
        if (type.isPrimitive()) return false;
        if (type.equals(String.class)
                || type.equals(Integer.class)
                || type.equals(Short.class)
                || type.equals(Long.class)
                || type.equals(Byte.class)
                || type.equals(Boolean.class)
                || type.equals(Double.class)
                || type.equals(Float.class)
                || type.equals(Character.class)
                || Map.class.isAssignableFrom(type)
                || List.class.isAssignableFrom(type)
                || Set.class.isAssignableFrom(type)
        ) {
            return false;
        }
        return true;
    }

    private boolean hasCollectionWriter(Getter getter) {
        Class type = getter.type;
        Type genericType = getter.genericType;
        if (!(genericType instanceof ParameterizedType)) return false;
        if (Map.class.isAssignableFrom(type)) {
            ParameterizedType pt = (ParameterizedType) genericType;
            Type valueType = pt.getActualTypeArguments()[1];
            Class valueClass = Types.getRawType(valueType);
            return hasNestedWriter(valueClass ,valueType);
        } else if (Collection.class.isAssignableFrom(type)) {
            ParameterizedType pt = (ParameterizedType)genericType;
            Class valueClass = Types.getRawType(pt.getActualTypeArguments()[0]);
            Type valueGenericType = pt.getActualTypeArguments()[0];
            return hasNestedWriter(valueClass, valueGenericType);
        } else {
            return false;
        }
    }

    private boolean hasNestedWriter(Class type, Type genericType) {
        if (isUserObject(type)) return true;
        if (!Map.class.isAssignableFrom(type)
                && !List.class.isAssignableFrom(type)
                && !Set.class.isAssignableFrom(type)
        ) {
            return false;
        }

        if (!(genericType instanceof ParameterizedType)) return false;

        if (Map.class.isAssignableFrom(type)) {
            ParameterizedType pt = (ParameterizedType)genericType;
            Class valueClass = Types.getRawType(pt.getActualTypeArguments()[1]);
            Type valueGenericType = pt.getActualTypeArguments()[1];
            return hasNestedWriter(valueClass, valueGenericType);
        } else if (Collection.class.isAssignableFrom(type)) {
            ParameterizedType pt = (ParameterizedType)genericType;
            Class valueClass = Types.getRawType(pt.getActualTypeArguments()[0]);
            Type valueGenericType = pt.getActualTypeArguments()[0];
            return hasNestedWriter(valueClass, valueGenericType);
        }
        return false;
    }


    private String fqn() {
        return fqn(targetType, targetGenericType);
    }

    private void findGetters(Class clz) {
        for (Method m : clz.getMethods()) {
            if (!isGetter(m))
                continue;
            Class paramType = m.getReturnType();
            Type paramGenericType = m.getGenericReturnType();
            String name;
            if (m.getName().startsWith("is")) {
                if (m.getName().length() > 3) {
                    name = Character.toLowerCase(m.getName().charAt(2)) + m.getName().substring(3);
                } else {
                    name = m.getName().substring(2).toLowerCase();
                }

            } else {
                if (m.getName().length() > 4) {
                    name = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
                } else {
                    name = m.getName().substring(3).toLowerCase();
                }
            }
            getters.add(new Getter(name, name, m, paramType, paramGenericType));
        }
        Collections.sort(getters, (getter, t1) -> getter.name.compareTo(t1.name));
    }


    static boolean isGetter(Method m) {
        return !Modifier.isStatic(m.getModifiers()) && ((m.getName().startsWith("get") && m.getName().length() > "get".length()) || (m.getName().startsWith("is")) && m.getName().length() > "is".length())
                && m.getParameterCount() == 0 && !m.getReturnType().equals(void.class)
                && !m.getDeclaringClass().equals(Object.class);
    }

    class Getter {
        String name;
        String property;
        Method method;
        Class type;
        Type genericType;

        public Getter(String name, String property, Method method, Class type, Type genericType) {
            this.name = name;
            this.property = property;
            this.method = method;
            this.type = type;
            this.genericType = genericType;
        }
    }
}
