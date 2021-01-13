package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.database.Cursor;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.WillClose;
import java.io.Closeable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ResultSet<T> implements Closeable {

    public final ContentType<T> type;
    private final Cursor cursor;
    private final Map<String, Integer> columnMap;

    public ResultSet(ContentType<T> type, Cursor cursor) {
        this.type = type;
        this.cursor = cursor;
        this.columnMap = Collections.unmodifiableMap(type.projection.stream()
                .map(s -> Pair.of(s, cursor.getColumnIndex(s)))
                .collect(Collectors.toMap(Pair::getLeft, Pair::getRight))
        );
        this.cursor.moveToFirst();
    }
    
    @WillClose
    public List<T> getDataList() {
        return this.getDataList(true);
    }
    
    public List<T> getDataList(boolean close) {
        if (this.cursor == null || this.cursor.isClosed()) {
            return ImmutableList.of();
        }
        List<T> list = new LinkedList<>();
        if (this.cursor.moveToFirst()) {
            do {
                list.add(this.type.getFromCursor(this.cursor, this.columnMap));
            } while (this.cursor.moveToNext());
        }
        this.cursor.moveToFirst();
        if (close) {
            this.close();
        } else {
            this.cursor.moveToFirst();
        }
        return ImmutableList.copyOf(list);
    }
    
    @WillClose
    public T head() {
        return this.head(true);
    }
    
    public T head(boolean close) {
        T t = null;
        if (this.cursor.moveToFirst()) {
            t = this.type.getFromCursor(this.cursor, this.columnMap);
        }
        if (close) {
            this.close();
        } else {
            this.cursor.moveToFirst();
        }
        return t;
    }
    
    @WillClose
    public void forEach(Consumer<T> action) {
        this.forEach(action, true);
    }
    
    public void forEach(Consumer<T> action, boolean close) {
        if (this.cursor == null || this.cursor.isClosed()) {
            return;
        }
        if (this.cursor.moveToFirst()) {
            do {
                action.accept(this.type.getFromCursor(this.cursor, this.columnMap));
            } while (this.cursor.moveToNext());
        }
        if (close) {
            this.close();
        } else {
            this.cursor.moveToFirst();
        }
    }

    @Override
    public void close() {
        if (!this.cursor.isClosed()) {
            this.cursor.close();
        }
    }
}
