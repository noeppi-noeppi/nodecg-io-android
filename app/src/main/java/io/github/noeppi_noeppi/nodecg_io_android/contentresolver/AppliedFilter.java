package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import org.apache.commons.lang3.tuple.Pair;

public class AppliedFilter<T> extends Pair<ContentFilter<T>, T> {

    public final ContentFilter<T> filter;
    public final T value;

    AppliedFilter(ContentFilter<T> filter, T value) {
        this.filter = filter;
        this.value = value;
    }

    @Override
    public ContentFilter<T> getLeft() {
        return this.filter;
    }

    @Override
    public T getRight() {
        return this.value;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }

    public String createFor(ContentType<?> type) {
        return this.filter.createFor(type, this.value);
    }
}
