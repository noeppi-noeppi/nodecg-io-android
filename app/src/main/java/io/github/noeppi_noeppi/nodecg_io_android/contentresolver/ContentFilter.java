package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.Telephony;
import android.telephony.SubscriptionInfo;
import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Mms;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Sms;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentType.*;

public class ContentFilter<T> {

    public static final ContentFilter<Void> EVERYTHING = new ContentFilter<>((t, v) -> null);
    public static final ContentFilter<Void> NOTHING = new ContentFilter<>((t, v) -> "NULL = NULL");
    
    public static final ContentFilter<AppliedFilter<?>> NOT = new ContentFilter<>((t, v) -> {
        if (v.filter == EVERYTHING) return NOTHING.createFor(t, null);
        else if (v.filter == NOTHING) return EVERYTHING.createFor(t, null);
        else return "NOT (" + v.createFor(t) + ")";
    });
    public static final ContentFilter<Pair<AppliedFilter<?>, AppliedFilter<?>>> AND = new ContentFilter<>(createCompoundFilter("AND"));
    public static final ContentFilter<Pair<AppliedFilter<?>, AppliedFilter<?>>> OR = new ContentFilter<>(createCompoundFilter("OR"));
    
    public static final ContentFilter<Long> BY_ID = new ContentFilter<>((t, v) -> BaseColumns._ID + " = " + v);
    public static final ContentFilter<Pair<String, String>> KEY_VALUE = new ContentFilter<>((t, v) -> v.getKey() + " = \"" + quote(v.getValue()) + "\"");
    
    public static final ContentFilter<Collection<Long>> BY_IDS = new ContentFilter<>((t, v) -> BaseColumns._ID + " IN " + v.stream().map(l -> Long.toString(l)).collect(Collectors.joining(",", "(", ")")));
    public static final ContentFilter<SubscriptionInfo> SUBSCRIPTION = new ContentFilter<>((t, v) -> {
        if (t.resultClass == Sms.class) {
            return Telephony.TextBasedSmsColumns.SUBSCRIPTION_ID + " = " + v.getSubscriptionId();
        } else if (t.resultClass == Mms.class) {
            return Telephony.BaseMmsColumns.SUBSCRIPTION_ID + " = " + v.getSubscriptionId();
        } else {
            throw new IllegalStateException("Illegal factory to be used with subscription filter: " + t.resultClass);
        }
    }, SMS_ALL, SMS_INBOX, SMS_OUTBOX, SMS_SENT, SMS_DRAFT, MMS_ALL, MMS_INBOX, MMS_OUTBOX, MMS_SENT, MMS_DRAFT);
    public static final ContentFilter<Long> THREAD = new ContentFilter<>((t, v) -> {
        if (t.resultClass == Sms.class) {
            return Telephony.TextBasedSmsColumns.THREAD_ID + " = " + v;
        } else if (t.resultClass == Mms.class) {
            return Telephony.BaseMmsColumns.THREAD_ID + " = " + v;
        } else {
            throw new IllegalStateException("Illegal factory to be used with subscription filter: " + t.resultClass);
        }
    }, SMS_ALL, SMS_INBOX, SMS_OUTBOX, SMS_SENT, SMS_DRAFT, MMS_ALL, MMS_INBOX, MMS_OUTBOX, MMS_SENT, MMS_DRAFT);
    
    public static final ContentFilter<Pair<String, String>> CONTACTS_RAW_ACCOUNT = new ContentFilter<>((t, v) ->
            ContactsContract.RawContacts.ACCOUNT_TYPE + " = \"" + quote(v.getLeft()) + "\" AND "
                    + ContactsContract.RawContacts.ACCOUNT_NAME + " = \"" + quote(v.getRight()) + "\"",
            CONTACT_RAW_ID);
    public static final ContentFilter<Long> BY_CONTACTS_ID = new ContentFilter<>((t, v) -> ContactsContract.RawContacts.CONTACT_ID + " = " + v,
            CONTACT_RAW_ID);
    
    public static final ContentFilter<String> CONTACTS_DATA_MIME = new ContentFilter<>((t, v) -> ContactsContract.Data.MIMETYPE + " = \"" + quote(v) + "\"",
            CONTACT_ID_FROM_DATA, CONTACT_NAME, CONTACT_PHONE, CONTACT_EMAIL, CONTACT_EVENT, CONTACT_NICKNAME, CONTACT_NOTES, CONTACT_ADDRESS);
    public static final ContentFilter<Long> BY_CONTACTS_RAW_ID = new ContentFilter<>((t, v) -> ContactsContract.Data.RAW_CONTACT_ID + " = " + v,
            CONTACT_ID_FROM_DATA, CONTACT_NAME, CONTACT_PHONE, CONTACT_EMAIL, CONTACT_EVENT, CONTACT_NICKNAME, CONTACT_NOTES, CONTACT_ADDRESS);
    
    
    // Null: Can be used on any content type
    @Nullable
    public final Set<ContentType<?>> availableTypes;
    private final BiFunction<ContentType<?>, T, String> factory;
    
    private ContentFilter(BiFunction<ContentType<?>, T, String> factory, ContentType<?>... types) {
        this.availableTypes = types.length <= 0 ? null : ImmutableSet.copyOf(types);
        this.factory = factory;
    }
    
    public String createFor(ContentType<?> type, T t) {
        if (this.availableTypes != null && !this.availableTypes.contains(type)) {
            throw new IllegalArgumentException("Failed to build filter: This filter is not available for the given content type: " + type.resultClass.getSimpleName());
        }
        return this.factory.apply(type, t);
    }
    
    public AppliedFilter<T> apply(T t) {
        return new AppliedFilter<>(this, t);
    }
    
    private static BiFunction<ContentType<?>, Pair<AppliedFilter<?>, AppliedFilter<?>>, String> createCompoundFilter(String join) {
        return (t, v) -> {
            if (v.getLeft().filter == EVERYTHING && v.getRight().filter == EVERYTHING) {
                return EVERYTHING.createFor(t, null);
            } else if (v.getRight().filter == EVERYTHING) {
                return v.getLeft().createFor(t);
            } else if (v.getLeft().filter == EVERYTHING) {
                return v.getRight().createFor(t);
            } else {
                return "(" + v.getLeft().createFor(t) + ") " + join + " (" + v.getRight().createFor(t) + ")";
            }
        };
    }
    
    private static String quote(String str) {
        return str.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    public static AppliedFilter<?> not(AppliedFilter<?> filter) {
        return NOT.apply(filter);
    }
    
    public static <T> AppliedFilter<?> not(ContentFilter<T> filter, T t) {
        return NOT.apply(filter.apply(t));
    }
    
    public static AppliedFilter<?> and(AppliedFilter<?> filter1, AppliedFilter<?> filter2) {
        if (filter1.filter == NOTHING || filter2.filter == NOTHING) {
            return NOTHING.apply(null);
        } else if (filter1.filter == EVERYTHING) {
            return filter2;
        } else if (filter2.filter == EVERYTHING){
            return filter1;
        } else {
            return AND.apply(Pair.of(filter1, filter2));
        }
    }

    public static <T> AppliedFilter<?> and(ContentFilter<T> filter1, T t, AppliedFilter<?> filter2) {
        return and(filter1.apply(t), filter2);
    }

    public static <U> AppliedFilter<?> and(AppliedFilter<?> filter1, ContentFilter<U> filter2, U u) {
        return and(filter1, filter2.apply(u));
    }

    public static <T, U> AppliedFilter<?> and(ContentFilter<T> filter1, T t, ContentFilter<U> filter2, U u) {
        return and(filter1.apply(t), filter2.apply(u));
    }
    
    public static AppliedFilter<?> or(AppliedFilter<?> filter1, AppliedFilter<?> filter2) {
        if (filter1.filter == EVERYTHING || filter2.filter == EVERYTHING) {
            return EVERYTHING.apply(null);
        } else if (filter1.filter == NOTHING) {
            return filter2;
        } else if (filter2.filter == NOTHING) {
            return filter1;
        } else {
            return OR.apply(Pair.of(filter1, filter2));
        }
    }

    public static <T> AppliedFilter<?> or(ContentFilter<T> filter1, T t, AppliedFilter<?> filter2) {
        return or(filter1.apply(t), filter2);
    }

    public static <U> AppliedFilter<?> or(AppliedFilter<?> filter1, ContentFilter<U> filter2, U u) {
        return or(filter1, filter2.apply(u));
    }

    public static <T, U> AppliedFilter<?> or(ContentFilter<T> filter1, T t, ContentFilter<U> filter2, U u) {
        return or(filter1.apply(t), filter2.apply(u));
    }
}
