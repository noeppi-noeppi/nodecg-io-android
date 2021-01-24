package io.github.noeppi_noeppi.nodecg_io_android;

import org.json.JSONException;

import java.util.Objects;
import java.util.function.Function;

public interface FailFunction<T, R> {

    R apply(T t) throws JSONException, FailureException;

    default <V> FailFunction<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> this.apply(before.apply(v));
    }

    default <V> FailFunction<V, R> compose(FailFunction<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> this.apply(before.apply(v));
    }

    default <V> FailFunction<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(this.apply(t));
    }

    default <V> FailFunction<T, V> andThen(FailFunction<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(this.apply(t));
    }
}
