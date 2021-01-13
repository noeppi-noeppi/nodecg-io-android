package io.github.noeppi_noeppi.nodecg_io_android.contentresolver;

import android.database.Cursor;
import android.net.Uri;
import android.provider.Telephony;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Mms;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Sms;

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


    public final Class<T> resultClass;
    public final Uri queryURI;
    public final List<String> projection;
    private final ContentFactory<T> factory;

    private ContentType(Uri queryURI, ContentFactory<T> factory) {
        this.resultClass = factory.resultClass;
        this.queryURI = queryURI;
        this.projection = factory.projection;
        this.factory = factory;
    }

    public T getFromCursor(Cursor cursor, Map<String, Integer> columnMap) {
        return this.factory.getFromCursor(cursor, columnMap);
    }
}
