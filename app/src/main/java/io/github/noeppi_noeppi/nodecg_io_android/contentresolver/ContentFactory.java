package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.*;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.MapType;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.BiFunction;

public class ContentFactory<T> {
    
    @SuppressWarnings("ConstantConditions")
    public static final ContentFactory<Long> ID = new ContentFactory<>(Long.class, (c, m) -> c.getLong(m.get(BaseColumns._ID)), BaseColumns._ID);
    public static final ContentFactory<Sms> SMS = createFrom(Sms.class);
    public static final ContentFactory<Mms> MMS = createFrom(Mms.class);
    public static final ContentFactory<MessageThread> MESSAGE_THREAD = createFrom(MessageThread.class);
    public static final ContentFactory<Recipient> RECIPIENT = createFrom(Recipient.class);
    
    public static final ContentFactory<Contact> CONTACT = createFrom(Contact.class);
    @SuppressWarnings("ConstantConditions")
    public static final ContentFactory<Long> CONTACT_ID_FROM_DATA = new ContentFactory<>(Long.class, (c, m) -> c.getLong(m.get(ContactsContract.Data.CONTACT_ID)), ContactsContract.Data.CONTACT_ID);
    public static final ContentFactory<ContactStatus> CONTACT_STATUS = createFrom(ContactStatus.class);
    public static final ContentFactory<ContactName> CONTACT_NAME = createFrom(ContactName.class);
    public static final ContentFactory<ContactPhone> CONTACT_PHONE = createFrom(ContactPhone.class);
    public static final ContentFactory<ContactEmail> CONTACT_EMAIL = createFrom(ContactEmail.class);
    public static final ContentFactory<ContactEvent> CONTACT_EVENT = createFrom(ContactEvent.class);
    public static final ContentFactory<ContactNickname> CONTACT_NICKNAME = createFrom(ContactNickname.class);
    public static final ContentFactory<ContactNotes> CONTACT_NOTES = createFrom(ContactNotes.class);
    public static final ContentFactory<ContactAddress> CONTACT_ADDRESS = createFrom(ContactAddress.class);
    
    public final Class<T> resultClass;
    public final List<String> projection;
    private final BiFunction<Cursor, Map<String, Integer>, T> factory;

    private ContentFactory(Class<T> resultClass, BiFunction<Cursor, Map<String, Integer>, T> factory, String... projection) {
        this.resultClass = resultClass;
        this.projection = ImmutableList.copyOf(projection);
        this.factory = factory;
    }

    public T getFromCursor(Cursor cursor, Map<String, Integer> columnMap) {
        return this.factory.apply(cursor, columnMap);
    }

    public static <T> ContentFactory<T> createFrom(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(DataClass.class)) {
            throw new IllegalStateException("Can't create content factory from non data class: " + clazz.getName());
        }
        Set<String> projection = new HashSet<>();
        Map<Field, Pair<String, MapType>> mutableMap = new HashMap<>();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            Mapping mapping = field.getAnnotation(Mapping.class);
            if (mapping != null) {
                field.setAccessible(true);
                String mapColumn = mapping.value();
                MapType mapType = mapping.map();
                if (mapType == MapType.AUTO) {
                    mapType = MapType.getAuto(field);
                }
                projection.add(mapColumn);
                mutableMap.put(field, Pair.of(mapColumn, mapType));
            }
        }
        Map<Field, Pair<String, MapType>> map = ImmutableMap.copyOf(mutableMap);
        return new ContentFactory<>(clazz, (cursor, columnMap) -> {
            try {
                T instance = clazz.newInstance();
                for (Map.Entry<Field, Pair<String, MapType>> mapping : map.entrySet()) {
                    if (columnMap.containsKey(mapping.getValue().getLeft())) {
                        //noinspection ConstantConditions
                        int columnId = columnMap.get(mapping.getValue().getLeft());
                        if (!cursor.isNull(columnId)) {
                            mapping.getKey().set(instance, mapping.getValue().getRight()
                                    .map(cursor, columnId));
                        }
                    }
                }
                return instance;
            } catch (ExceptionInInitializerError e) {
                Throwable t = e.getException();
                if (t instanceof RuntimeException) {
                    throw (RuntimeException) t;
                } else if (t instanceof Error) {
                    throw (Error) t;
                } else {
                    throw new RuntimeException(t);
                }
            } catch (ReflectiveOperationException e) {
                throw new RuntimeException(e);
            }
        }, projection.toArray(new String[]{}));
    }
}
