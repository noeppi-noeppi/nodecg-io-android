package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping;

import android.database.Cursor;
import com.google.common.collect.ImmutableSet;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.BiFunction;

public enum MapType {

    AUTO((c, i) -> {
        throw new NoSuchElementException("Can't directly call AUTO MapType.");
    }, void.class),
    BOOL((c, i) -> c.getInt(i) == 0, boolean.class, Boolean.class),
    SHORT(Cursor::getShort, short.class, Short.class),
    INTEGER(Cursor::getInt, int.class, Integer.class),
    LONG(Cursor::getLong, long.class, Long.class),
    FLOAT(Cursor::getFloat, float.class, Float.class),
    DOUBLE(Cursor::getDouble, double.class, Double.class),
    STRING(Cursor::getString, String.class),
    BYTES(Cursor::getBlob, byte[].class),
    DATE((c, i) -> new Date(c.getLong(i)), Date.class);

    private final Set<Class<?>> classes;
    private final BiFunction<Cursor, Integer, Object> mapper;

    MapType(BiFunction<Cursor, Integer, Object> mapper, Class<?>... classes) {
        this.mapper = mapper;
        this.classes = ImmutableSet.copyOf(classes);
    }

    public Object map(Cursor cursor, int columnId) {
        return this.mapper.apply(cursor, columnId);
    }

    public static MapType getAuto(Field field) {
        Class<?> clazz = field.getType();
        for (MapType type : MapType.values()) {
            if (type.classes.contains(clazz)) {
                return type;
            }
        }
        throw new IllegalStateException("Can't infer MapType for field: " + field);
    }
}
