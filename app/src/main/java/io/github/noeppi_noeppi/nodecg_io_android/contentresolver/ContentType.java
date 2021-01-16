package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Telephony;
import com.google.common.collect.ImmutableList;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.*;

import java.util.List;
import java.util.Map;

public class ContentType<T> {
    
    public static final ContentType<Sms> SMS_ALL = new ContentType<>(Telephony.Sms.CONTENT_URI, ContentFactory.SMS);
    public static final ContentType<Sms> SMS_INBOX = new ContentType<>(Telephony.Sms.Inbox.CONTENT_URI, ContentFactory.SMS);
    public static final ContentType<Sms> SMS_OUTBOX = new ContentType<>(Telephony.Sms.Outbox.CONTENT_URI, ContentFactory.SMS);
    public static final ContentType<Sms> SMS_SENT = new ContentType<>(Telephony.Sms.Sent.CONTENT_URI, ContentFactory.SMS);
    public static final ContentType<Sms> SMS_DRAFT = new ContentType<>(Telephony.Sms.Draft.CONTENT_URI, ContentFactory.SMS);

    public static final ContentType<Mms> MMS_ALL = new ContentType<>(Telephony.Mms.CONTENT_URI, ContentFactory.MMS);
    public static final ContentType<Mms> MMS_INBOX = new ContentType<>(Telephony.Mms.Inbox.CONTENT_URI, ContentFactory.MMS);
    public static final ContentType<Mms> MMS_OUTBOX = new ContentType<>(Telephony.Mms.Outbox.CONTENT_URI, ContentFactory.MMS);
    public static final ContentType<Mms> MMS_SENT = new ContentType<>(Telephony.Mms.Sent.CONTENT_URI, ContentFactory.MMS);
    public static final ContentType<Mms> MMS_DRAFT = new ContentType<>(Telephony.Mms.Draft.CONTENT_URI, ContentFactory.MMS);

    public static final ContentType<MessageThread> MESSAGE_THREAD = new ContentType<>(ImmutableList.of(
            Telephony.Threads.CONTENT_URI.buildUpon().appendQueryParameter("simple", "true").build(),
            Uri.parse("content://mms-sms/complete-conversations").buildUpon().appendQueryParameter("simple", "true").build(),
            Telephony.Threads.CONTENT_URI,
            Uri.parse("content://mms-sms/complete-conversations")
    ), ContentFactory.MESSAGE_THREAD);

    public static final ContentType<Recipient> RECIPIENT = new ContentType<>(Uri.parse("content://mms-sms/canonical-addresses"), ContentFactory.RECIPIENT);
    
    public static final ContentType<Contact> CONTACT = new ContentType<>(ContactsContract.Contacts.CONTENT_URI, ContentFactory.CONTACT);
    public static final ContentType<ContactStatus> CONTACT_STATUS = new ContentType<>(ContactsContract.Contacts.CONTENT_URI, ContentFactory.CONTACT_STATUS);
    public static final ContentType<ContactName> CONTACT_NAME = new ContentType<>(ContactsContract.Data.CONTENT_URI, ContentFactory.CONTACT_NAME);

    public final Class<T> resultClass;

    // This needs to be a list because some URIs don't work everywhere and you need ti use a different...
    public final List<Uri> queryURI;
    public final List<String> projection;
    private final ContentFactory<T> factory;

    private ContentType(Uri queryURI, ContentFactory<T> factory) {
        this(ImmutableList.of(queryURI), factory);
    }

    private ContentType(ImmutableList<Uri> queryURI, ContentFactory<T> factory) {
        this.resultClass = factory.resultClass;
        this.queryURI = queryURI;
        this.projection = factory.projection;
        this.factory = factory;
    }

    public T getFromCursor(Cursor cursor, Map<String, Integer> columnMap) {
        return this.factory.getFromCursor(cursor, columnMap);
    }
}
