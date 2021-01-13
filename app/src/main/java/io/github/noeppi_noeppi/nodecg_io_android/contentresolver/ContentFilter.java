package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.provider.BaseColumns;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Mms;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Sms;

import javax.annotation.Nullable;
import java.util.Set;
import java.util.function.BiFunction;

import static io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentType.*;

public class ContentFilter<T> {
    
    public static final ContentFilter<Void> EVERYTHING = new ContentFilter<>((t, v) -> null);
    public static final ContentFilter<Long> BY_ID = new ContentFilter<>((t, v) -> BaseColumns._ID + " = " + v);
    public static final ContentFilter<SubscriptionInfo> SUBSCRIPTION = new ContentFilter<>((t, v) -> {
        if (t.resultClass == Sms.class) {
            return Telephony.TextBasedSmsColumns.SUBSCRIPTION_ID + " = " + v.getSubscriptionId();
        } else if (t.resultClass == Mms.class) {
            return Telephony.BaseMmsColumns.SUBSCRIPTION_ID + " = " + v.getSubscriptionId();
        } else {
            throw new IllegalStateException("Illegal factory to be used with subscription filter: " + t.resultClass);
        }
    }, SMS_ALL, SMS_INBOX, SMS_OUTBOX, SMS_SENT, SMS_DRAFT, MMS_ALL, MMS_INBOX, MMS_OUTBOX, MMS_SENT, MMS_DRAFT);
            
    // Null: Can be used on any content type
    @Nullable
    public final Set<ContentType<?>> availableTypes;
    private final BiFunction<ContentType<?>, T, String> factory;
    
    private ContentFilter(BiFunction<ContentType<?>, T, String> factory, ContentType<?>... types) {
        this.availableTypes = types.length <= 0 ? null : ImmutableSet.copyOf(types);
        this.factory = factory;
    }
    
    public String createFor(ContentType<?> type, T t) {
        return this.factory.apply(type, t);
    }
    
    public AppliedFilter<T> apply(T t) {
        return new AppliedFilter<>(this, t);
    }
}
