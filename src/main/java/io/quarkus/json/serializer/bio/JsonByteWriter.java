package io.quarkus.json.serializer.bio;

import io.quarkus.json.IntChar;

import java.nio.charset.Charset;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class JsonByteWriter implements JsonWriter {
    final ByteWriter writer;

    static final byte[] TRUE = {'t', 'r', 'u', 'e'};
    static final byte[] FALSE = {'f', 'a', 'l', 's', 'e'};

    public static Charset UTF8 = Charset.forName("UTF-8");

    public JsonByteWriter(ByteWriter writer) {
        this.writer = writer;
    }

    @Override
    public void writeComma() {
        writer.write(IntChar.INT_COMMA);
    }

    @Override
    public void write(short val) {
        write((long)val);
    }

    @Override
    public void write(int val) {
        write((long)val);
    }

    @Override
    public void write(long x) {
        int places = 1;
        long n = x / 10;
        for (; n != 0; n /= 10) places *= 10;

        StringBuilder builder = new StringBuilder();
        if (x < 0) {
            builder.append('-');
            for (int place = places; place >=1; place /= 10) {
                int i = (int)(x / place);
                writer.write('0' - i);
                x -= i * place;
            }
        } else {
            for (int place = places; place >=1; place /= 10) {
                int i = (int)(x / place);
                writer.write('0' + i);
                x -= i * place;
            }
        }
    }

    @Override
    public void write(boolean val) {
        if (val) writer.write(TRUE);
        else writer.write(FALSE);
    }

    @Override
    public void write(byte val) {
        write((long)val);
    }

    @Override
    public void write(float val) {
        writer.write(Float.toString(val).getBytes(UTF8));
    }

    @Override
    public void write(double val) {
        writer.write(Double.toString(val).getBytes(UTF8));
    }

    @Override
    public void write(char val) {
        writer.write(IntChar.INT_QUOTE);
        writer.write(val);
        writer.write(IntChar.INT_QUOTE);
    }

    @Override
    public void write(Character val) {
        write(val.charValue());
    }

    @Override
    public void write(Short val) {
        write(val.longValue());

    }

    @Override
    public void write(Integer val) {
        write(val.longValue());

    }

    @Override
    public void write(Long val) {
        write(val.longValue());

    }

    @Override
    public void write(Boolean val) {
        write(val.booleanValue());
    }

    @Override
    public void write(Byte val) {
        write(val.longValue());
    }

    @Override
    public void write(Float val) {
        write(val.floatValue());

    }

    @Override
    public void write(Double val) {
        write(val.doubleValue());

    }

    @Override
    public void write(String val) {
        writer.write(IntChar.INT_QUOTE);
        writer.write(val.getBytes(UTF8));
        writer.write(IntChar.INT_QUOTE);
    }

    @Override
    public void write(Object val, ObjectWriter writer) {
        this.writer.write(IntChar.INT_LCURLY);
        writer.write(this, val);
        this.writer.write(IntChar.INT_RCURLY);
    }

    @Override
    public void writeProperty(String name, char val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, short val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, int val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, long val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, boolean val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, byte val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, float val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, double val) {
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Character val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Short val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Integer val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Long val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Boolean val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Byte val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Float val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Double val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, String val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        write(val);
    }

    @Override
    public void writeProperty(String name, Object val, ObjectWriter writer) {
        if (val == null) return;
        write(name);
        this.writer.write(IntChar.INT_COLON);
        write(val, writer);
    }

    @Override
    public void writeProperty(String name, Map val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        writer.write(IntChar.INT_LCURLY);
        Set<Map.Entry<Object, Object>> set = val.entrySet();
        boolean first = true;
        for (Map.Entry<Object, Object> entry : set) {
            if (first) first = false;
            else writer.write(IntChar.INT_COMMA);
            writePropertyName(entry.getKey());
            writer.write(IntChar.INT_COLON);
            writePropertyValue(entry.getValue());
        }
        writer.write(IntChar.INT_RCURLY);

    }

    private void writePropertyName(Object obj) {
        if (obj instanceof String) {
            write((String)obj);
            return;
        }
        if (obj instanceof Character) {
            write((Character)obj);
            return;
        }
        if (obj instanceof Short) {
            writer.write(IntChar.INT_QUOTE);
            write((Short)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Integer) {
            writer.write(IntChar.INT_QUOTE);
            write((Integer)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Long) {
            writer.write(IntChar.INT_QUOTE);
            write((Long)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Byte) {
            writer.write(IntChar.INT_QUOTE);
            write((Byte)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Boolean) {
            writer.write(IntChar.INT_QUOTE);
            write((Boolean)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Float) {
            writer.write(IntChar.INT_QUOTE);
            write((Short)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        if (obj instanceof Double) {
            writer.write(IntChar.INT_QUOTE);
            write((Short)obj);
            writer.write(IntChar.INT_QUOTE);
            return;
        }
        throw new IllegalStateException("Expecting a primitive or string for Map key.");

    }

    private void writePropertyValue(Object obj) {
        if (obj instanceof String) {
            write((String)obj);
            return;
        }
        if (obj instanceof Character) {
            write((Character)obj);
            return;
        }
        if (obj instanceof Short) {
            write((Short)obj);
            return;
        }
        if (obj instanceof Integer) {
            write((Integer)obj);
            return;
        }
        if (obj instanceof Long) {
            write((Long)obj);
            return;
        }
        if (obj instanceof Byte) {
            write((Byte)obj);
            return;
        }
        if (obj instanceof Boolean) {
            write((Boolean)obj);
            return;
        }
        if (obj instanceof Float) {
            write((Short)obj);
            return;
        }
        if (obj instanceof Double) {
            write((Short)obj);
            return;
        }
        throw new IllegalStateException("Expecting a primitive or string value.  Make sure to use generics for maps and collections");

    }


    @Override
    public void writeProperty(String name, Collection val) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        writer.write(IntChar.INT_LBRACKET);
        boolean first = true;
        for (Object item : val) {
            if (first) first = false;
            else writer.write(IntChar.INT_COMMA);
            writePropertyValue(item);
        }
        writer.write(IntChar.INT_RBRACKET);

    }

    @Override
    public void writeProperty(String name, Map val, ObjectWriter objectWriter) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        writer.write(IntChar.INT_LCURLY);
        Set<Map.Entry<Object, Object>> set = val.entrySet();
        boolean first = true;
        for (Map.Entry<Object, Object> entry : set) {
            if (first) first = false;
            else writer.write(IntChar.INT_COMMA);
            writePropertyName(entry.getKey());
            writer.write(IntChar.INT_COLON);
            write(entry.getValue(), objectWriter);
        }
        writer.write(IntChar.INT_RCURLY);
    }

    @Override
    public void writeProperty(String name, Collection val, ObjectWriter objectWriter) {
        if (val == null) return;
        write(name);
        writer.write(IntChar.INT_COLON);
        writer.write(IntChar.INT_LBRACKET);
        boolean first = true;
        for (Object item : val) {
            if (first) first = false;
            else writer.write(IntChar.INT_COMMA);
            write(item, objectWriter);
        }
        writer.write(IntChar.INT_RBRACKET);
    }
}
