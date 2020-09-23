package io.quarkus.json.generator.buffered;

import io.quarkus.gizmo.AssignableResultHandle;
import io.quarkus.gizmo.ClassCreator;
import io.quarkus.gizmo.ClassOutput;
import io.quarkus.gizmo.FieldCreator;
import io.quarkus.gizmo.FieldDescriptor;
import io.quarkus.gizmo.MethodCreator;
import io.quarkus.gizmo.MethodDescriptor;
import io.quarkus.gizmo.ResultHandle;
import io.quarkus.json.generator.Types;
import io.quarkus.json.serializer.bio.JsonWriter;
import io.quarkus.json.serializer.bio.ObjectWriter;

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

import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

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
        staticConstructor.returnValue(null);
    }

    private void writeMethod() {
        MethodCreator method = creator.getMethodCreator("write", void.class, JsonWriter.class, Object.class);
        ResultHandle jsonWriter = method.getMethodParam(0);
        AssignableResultHandle target = method.createVariable(targetType);
        method.assign(target, method.getMethodParam(1));
        // todo support an interface as type
        boolean first = true;
        for (Getter getter : getters) {
            if (first) first = false;
            else {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeComma", void.class), jsonWriter);
            }

            if (getter.type.equals(int.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, int.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Integer.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Integer.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(short.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, short.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Short.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Short.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(long.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, long.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Long.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Long.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(byte.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, byte.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Byte.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Byte.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(boolean.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Boolean.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Boolean.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(float.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, float.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Float.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Float.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(double.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, double.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Double.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Double.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(char.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, char.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(Character.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Character.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (getter.type.equals(String.class)) {
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, String.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
            } else if (Map.class.isAssignableFrom(getter.type)) {
                Class valueType = null;
                if (getter.genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)getter.genericType;
                    valueType = Types.getRawType(pt.getActualTypeArguments()[1]);
                }
                if (valueType == null || !isUserObject(valueType)) {
                    method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Map.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
                } else {
                    needed.put(getter.type, getter.genericType);
                    method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Map.class, ObjectWriter.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            method.readStaticField(FieldDescriptor.of(fqn(valueType, valueType), "SERIALIZER", fqn(valueType, valueType)))
                    );
                }
            } else if (Collection.class.isAssignableFrom(getter.type)) {
                Class valueType = null;
                if (getter.genericType instanceof ParameterizedType) {
                    ParameterizedType pt = (ParameterizedType)getter.genericType;
                    valueType = Types.getRawType(pt.getActualTypeArguments()[0]);
                }
                if (valueType == null || !isUserObject(valueType)) {
                    method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Collection.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target));
                } else {
                    needed.put(getter.type, getter.genericType);
                    method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Collection.class, ObjectWriter.class), jsonWriter,
                            method.load(getter.name),
                            method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                            method.readStaticField(FieldDescriptor.of(fqn(valueType, valueType), "SERIALIZER", fqn(valueType, valueType)))
                    );
                }
            } else {
                needed.put(getter.type, getter.genericType);
                method.invokeInterfaceMethod(MethodDescriptor.ofMethod(JsonWriter.class, "writeProperty", void.class, String.class, Object.class, ObjectWriter.class), jsonWriter,
                        method.load(getter.name),
                        method.invokeVirtualMethod(MethodDescriptor.ofMethod(targetType, getter.method.getName(), getter.type), target),
                        method.readStaticField(FieldDescriptor.of(fqn(getter.type, getter.genericType), "SERIALIZER", fqn(getter.type, getter.genericType)))
                );
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
        ) {
            return false;
        }
        return true;
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
            if (m.getName().length() > 4) {
                name = Character.toLowerCase(m.getName().charAt(3)) + m.getName().substring(4);
            } else {
                name = m.getName().substring(3).toLowerCase();
            }
            getters.add(new Getter(name, m, paramType, paramGenericType));
        }
        Collections.sort(getters, (getter, t1) -> getter.name.compareTo(t1.name));
    }


    static boolean isGetter(Method m) {
        return !Modifier.isStatic(m.getModifiers()) && m.getName().startsWith("get") && m.getName().length() > "get".length()
                && m.getParameterCount() == 0 && !m.getReturnType().equals(void.class)
                && !m.getDeclaringClass().equals(Object.class);
    }

    class Getter {
        String name;
        Method method;
        Class type;
        Type genericType;

        public Getter(String name, Method method, Class type, Type genericType) {
            this.name = name;
            this.method = method;
            this.type = type;
            this.genericType = genericType;
        }
    }
}
