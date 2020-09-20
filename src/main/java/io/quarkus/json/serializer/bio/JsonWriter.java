package io.quarkus.json.serializer.bio;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface JsonWriter {
    void writeComma();
    void write(short val);
    void write(int val);
    void write(long val);
    void write(boolean val);
    void write(byte val);
    void write(float val);
    void write(double val);
    void write(char val);
    void write(Character val);
    void write(Short val);
    void write(Integer val);
    void write(Long val);
    void write(Boolean val);
    void write(Byte val);
    void write(Float val);
    void write(Double val);
    void write(String val);
    void write(Object obj, ObjectWriter writer);


    void writeProperty(String name, char val);
    void writeProperty(String name, short val);
    void writeProperty(String name, int val);
    void writeProperty(String name, long val);
    void writeProperty(String name, boolean val);
    void writeProperty(String name, byte val);
    void writeProperty(String name, float val);
    void writeProperty(String name, double val);
    void writeProperty(String name, Character val);
    void writeProperty(String name, Short val);
    void writeProperty(String name, Integer val);
    void writeProperty(String name, Long val);
    void writeProperty(String name, Boolean val);
    void writeProperty(String name, Byte val);
    void writeProperty(String name, Float val);
    void writeProperty(String name, Double val);
    void writeProperty(String name, String val);
    void writeProperty(String name, Object obj, ObjectWriter writer);


    void writeProperty(String name, Map map);
    void writeProperty(String name, List list);
    void writeProperty(String name, Set set);
    void writeProperty(String name, Map map, ObjectWriter writer);
    void writeProperty(String name, List list, ObjectWriter writer);
    void writeProperty(String name, Set set, ObjectWriter writer);
}
