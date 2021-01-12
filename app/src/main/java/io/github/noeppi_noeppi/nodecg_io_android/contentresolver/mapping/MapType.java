package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping;

import android.database.Cursor;

import java.lang.reflect.Field;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;

public enum MapType {
    
    AUTO((c, i) -> { throw new NoSuchElementException("Can't directly call AUTO MapType."); }),
    BOOL((c, i) -> c.getInt(i) == 0),
    SHORT(Cursor::getShort),
    INTEGER(Cursor::getInt),
    LONG(Cursor::getLong),
    FLOAT(Cursor::getFloat),
    DOUBLE(Cursor::getDouble),
    STRING(Cursor::getString),
    BYTES(Cursor::getBlob);
    
    private final BiFunction<Cursor, Integer, Object> mapper;

    MapType(BiFunction<Cursor, Integer, Object> mapper) {
        this.mapper = mapper;
    }
    
    public Object map(Cursor cursor, int columnId) {
        return this.mapper.apply(cursor, columnId);
    }
    
    public static MapType getAuto(Field field) {
        Class<?> clazz = field.getType();
        if (clazz == boolean.class || clazz == Boolean.class) {
            return BOOL;
        } else if (clazz == short.class || clazz == Short.class) {
            return SHORT;
        } else if (clazz == int.class || clazz == Integer.class) {
            return INTEGER;
        } else if (clazz == long.class || clazz == Long.class) {
            return LONG;
        } else if (clazz == float.class || clazz == Float.class) {
            return FLOAT;
        } else if (clazz == double.class || clazz == Double.class) {
            return DOUBLE;
        } else if (clazz == String.class) {
            return STRING;
        } else if (clazz == byte[].class) {
            return BYTES;
        } else {
            throw new IllegalStateException("Can't infer MapType for field: " + field);
        }
    }
}
