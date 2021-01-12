package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import com.google.common.collect.ImmutableSet;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.Function;

public class ContentFilter<T> {
    
    public static final ContentFilter<Void> EVERYTHING = new ContentFilter<>(v -> null);
            
    // Null: Can be used on any content type
    @Nullable
    public final Set<ContentType<?>> availableTypes;
    private final Function<T, String> factory;
    
    private ContentFilter(Function<T, String> factory, ContentType<?>... types) {
        this.availableTypes = types.length <= 0 ? null : ImmutableSet.copyOf(types);
        this.factory = factory;
    }
    
    public String createFor(T t) {
        return this.factory.apply(t);
    }
}
