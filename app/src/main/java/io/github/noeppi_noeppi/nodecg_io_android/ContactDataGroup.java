package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import android.provider.ContactsContract;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.AppliedFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentProvider;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentType;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.*;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.apache.commons.lang3.tuple.Pair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;

public class ContactDataGroup<T extends ToJSON> {

    private static final Map<String, ContactDataGroup<?>> groups = new HashMap<>();

    public static final ContactDataGroup<ContactName> NAME = new ContactDataGroup<>("name", ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE, ContentType.CONTACT_NAME, true, ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME);
    public static final ContactDataGroup<ContactPhone> PHONE = new ContactDataGroup<>("phone", ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE, ContentType.CONTACT_PHONE, false, ContactsContract.CommonDataKinds.Phone.NUMBER, ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER);
    public static final ContactDataGroup<ContactEmail> EMAIL = new ContactDataGroup<>("email", ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE, ContentType.CONTACT_EMAIL, false, ContactsContract.CommonDataKinds.Email.ADDRESS, ContactsContract.CommonDataKinds.Email.DISPLAY_NAME);
    public static final ContactDataGroup<ContactEvent> EVENT = new ContactDataGroup<>("event", ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE, ContentType.CONTACT_EVENT, false, ContactsContract.CommonDataKinds.Event.START_DATE);
    public static final ContactDataGroup<ContactNickname> NICKNAME = new ContactDataGroup<>("nickname", ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE, ContentType.CONTACT_NICKNAME, false, ContactsContract.CommonDataKinds.Nickname.NAME);
    public static final ContactDataGroup<ContactNotes> NOTES = new ContactDataGroup<>("notes", ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE, ContentType.CONTACT_NOTES, true);
    public static final ContactDataGroup<ContactAddress> ADDRESS = new ContactDataGroup<>("address", ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE, ContentType.CONTACT_ADDRESS, false, ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, ContactsContract.CommonDataKinds.StructuredPostal.STREET, ContactsContract.CommonDataKinds.StructuredPostal.POBOX, ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE, ContactsContract.CommonDataKinds.StructuredPostal.CITY);

    public final String mime;
    public final List<String> searchColumns;
    public final ContentType<T> content;
    public final boolean singleton;

    public ContactDataGroup(String id, String mime, ContentType<T> content, boolean singleton, String... searchColumns) {
        this.mime = mime;
        this.searchColumns = ImmutableList.copyOf(searchColumns);
        this.content = content;
        this.singleton = singleton;
        groups.put(id, this);
    }

    public void sendResult(Context ctx, @SuppressWarnings("OptionalUsedAsFieldOrParameterType") OptionalLong rawId, Feedback feedback) throws JSONException, FailureException {
        if (!rawId.isPresent()) {
            if (this.singleton) {
                feedback.sendFeedback(new JSONObject());
            } else {
                feedback.sendFeedback("data", new JSONArray());
            }
        } else {
            List<T> resultSet = new ContentProvider<>(ctx, this.content).query(ContentFilter.and(
                    ContentFilter.CONTACTS_DATA_MIME, this.mime,
                    ContentFilter.BY_CONTACTS_RAW_ID, rawId.getAsLong()
            )).getDataList();
            if (this.singleton) {
                if (resultSet.isEmpty()) {
                    feedback.sendFeedback(new JSONObject());
                } else {
                    JSONObject json = resultSet.get(0).toJSON();
                    if (json == null) {
                        feedback.sendFeedback(new JSONObject());
                    } else {
                        feedback.sendFeedback("data", json);
                    }
                }
            } else {
                JSONArray array = new JSONArray();
                for (T t : resultSet) {
                    JSONObject json = t.toJSON();
                    if (json != null) {
                        array.put(json);
                    }
                }
                feedback.sendFeedback("data", array);
            }
        }
    }

    public List<Contact> findContacts(Context ctx, String value) {
        if (this.searchColumns.isEmpty()) {
            return ImmutableList.of();
        }
        AppliedFilter<?> orFilter = ContentFilter.NOTHING.apply(null);
        for (String filter : this.searchColumns) {
            orFilter = ContentFilter.or(orFilter, ContentFilter.KEY_VALUE, Pair.of(filter, value));
        }
        AppliedFilter<?> filter = ContentFilter.and(
                ContentFilter.CONTACTS_DATA_MIME.apply(this.mime),
                orFilter
        );
        List<Long> resultSet = new ContentProvider<>(ctx, ContentType.CONTACT_ID_FROM_DATA).query(filter).getDataList();
        return new ContentProvider<>(ctx, ContentType.CONTACT).query(ContentFilter.BY_IDS, ImmutableSet.copyOf(resultSet)).getDataList();
    }

    public static OptionalLong getRawContactId(Context ctx, long contactId, Pair<String, String> rawContactAccount) {
        Long rawId = new ContentProvider<>(ctx, ContentType.CONTACT_RAW_ID).query(ContentFilter.and(
                ContentFilter.BY_CONTACTS_ID, contactId,
                ContentFilter.CONTACTS_RAW_ACCOUNT, rawContactAccount
        )).head();
        if (rawId == null) {
            return OptionalLong.empty();
        } else {
            return OptionalLong.of(rawId);
        }
    }

    @Nullable
    public static ContactDataGroup<?> byId(String id) {
        return groups.get(id);
    }
}
