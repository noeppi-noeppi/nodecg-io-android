package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.SQLException;
import android.net.Uri;

import java.util.HashSet;
import java.util.Set;

public class ContentProvider<T> {

    protected final Context ctx;
    protected final ContentType<T> type;
    private final ContentResolver resolver;
    private final Set<ContentObserver> registeredObservers;
    private final Uri queryURI;

    public ContentProvider(Context ctx, ContentType<T> type) {
        this.ctx = ctx;
        this.type = type;
        this.resolver = ctx.getContentResolver();
        this.registeredObservers = new HashSet<>();
        if (type.queryURI.size() == 1) {
            this.queryURI = type.queryURI.get(0);
        } else {
            Uri uriToUse = type.queryURI.get(0);
            for (Uri uri : type.queryURI) {
                try {
                    this.resolver.query(uri, type.projection.toArray(new String[]{}), ContentFilter.NOTHING.createFor(type, null), null, null).close();
                } catch (SQLException e) {
                    continue;
                }
                uriToUse = uri;
                break;
            }
            this.queryURI = uriToUse;
        }
    }

    public void addObserver(ContentObserver observer, boolean notifyForDescendants) {
        this.resolver.registerContentObserver(this.queryURI, notifyForDescendants, observer);
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

    public <F> ResultSet<T> query(AppliedFilter<F> filter) {
        return this.query(filter.getLeft(), filter.getRight());
    }

    public <F> ResultSet<T> query(ContentFilter<F> filter, F value) {
        Cursor cursor = this.resolver.query(this.queryURI, this.type.projection.toArray(new String[]{}), filter.createFor(this.type, value), null, null);
        return new ResultSet<>(this.type, cursor);
    }
}
