package io.quarkus.json.serializer;

public class Types {
    /**
     * Gizmo doesn't have a typecast.  This is a workaround.
     *
     * @param obj
     * @param <T>
     * @return
     */
    static <T> T typecast(Object obj) {
        return (T)obj;
    }
}
