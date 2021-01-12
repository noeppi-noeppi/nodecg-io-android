package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.database.Cursor;
import android.util.Pair;
import com.google.common.collect.ImmutableList;

import javax.annotation.WillNotClose;
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
                .map(s -> Pair.create(s, cursor.getColumnIndex(s)))
                .collect(Collectors.toMap(p -> p.first, p -> p.second))
        );
        this.cursor.moveToFirst();
    }
    
    @WillNotClose
    public List<T> getDataList() {
        return this.getDataList(false);
    }
    
    public List<T> getDataList(boolean close) {
        if (this.cursor == null || this.cursor.isClosed()) {
            return ImmutableList.of();
        }
        List<T> list = new LinkedList<>();
        while (this.cursor.moveToNext()) {
            list.add(this.type.getFromCursor(this.cursor, this.columnMap));
        }
        this.cursor.moveToFirst();
        if (close) {
            this.close();
        } else {
            this.cursor.moveToFirst();
        }
        return ImmutableList.copyOf(list);
    }
    
    @WillNotClose
    public void forEach(Consumer<T> action) {
        if (this.cursor == null || this.cursor.isClosed()) {
            return;
        }
        
        while (this.cursor.moveToNext()) {
            action.accept(this.type.getFromCursor(this.cursor, this.columnMap));
        }
        this.cursor.moveToFirst();
    }

    @Override
    public void close() {
        if (!this.cursor.isClosed()) {
            this.cursor.close();
        }
    }
}
