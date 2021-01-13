package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import org.apache.commons.lang3.tuple.Pair;

public class AppliedFilter<T> extends Pair<ContentFilter<T>, T> {

    private final ContentFilter<T> left;
    private final T right;

    AppliedFilter(ContentFilter<T> left, T right) {
        this.left = left;
        this.right = right;
    }

    @Override
    public ContentFilter<T> getLeft() {
        return this.left;
    }

    @Override
    public T getRight() {
        return this.right;
    }

    @Override
    public T setValue(T value) {
        throw new UnsupportedOperationException();
    }
}
