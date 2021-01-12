package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;

import java.util.HashSet;
import java.util.Set;

public class ContentProvider<T> {

    protected final Context ctx;
    protected final ContentType<T> type;
    private final ContentResolver resolver;
    private final Set<ContentObserver> registeredObservers;
    
    public ContentProvider(Context ctx, ContentType<T> type) {
        this.ctx = ctx;
        this.type = type;
        this.resolver = ctx.getContentResolver();
        this.registeredObservers = new HashSet<>();
    }
    
    public void addObserver(ContentObserver observer, boolean notifyForDescendants) {
        this.resolver.registerContentObserver(this.type.queryURI, notifyForDescendants, observer);
        this.registeredObservers.add(observer);
    }
    
    public void removeObserver(ContentObserver observer) {
        this.resolver.unregisterContentObserver(observer);
        this.registeredObservers.remove(observer);
    }
    
    public void finish() {
        this.registeredObservers.forEach(this.resolver::unregisterContentObserver);
        this.registeredObservers.clear();
    }

    public ResultSet<T> query() {
        return this.query(ContentFilter.EVERYTHING, null);
    }
    
    public <F> ResultSet<T> query(ContentFilter<F> filter, F value) {
        if (filter.availableTypes != null && !filter.availableTypes.contains(this.type)) {
            throw new IllegalArgumentException("Invalid filter given for query in ContentProvider");
        }
        Cursor cursor = this.resolver.query(this.type.queryURI, this.type.projection.toArray(new String[]{}), filter.createFor(value), null, null);
        return new ResultSet<>(this.type, cursor);
    }
}
