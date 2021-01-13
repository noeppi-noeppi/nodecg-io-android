package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.BaseColumns;
import android.provider.Telephony;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@DataClass
public class Sms {

    @Mapping(BaseColumns._ID)
    public long _id;
    
    @Mapping(Telephony.TextBasedSmsColumns.ADDRESS)
    public String address;

    @Mapping(Telephony.TextBasedSmsColumns.SUBJECT)
    public String subject;
    
    @Mapping(Telephony.TextBasedSmsColumns.BODY)
    public String body = "";

    @Mapping(value = Telephony.TextBasedSmsColumns.DATE)
    public Date receivedDate;

    @Mapping(value = Telephony.TextBasedSmsColumns.DATE_SENT)
    public Date sentDate;
    
    @Mapping(Telephony.TextBasedSmsColumns.THREAD_ID)
    public long threadId;
    
    @Mapping(Telephony.TextBasedSmsColumns.READ)
    public boolean read;
    
    @Mapping(Telephony.TextBasedSmsColumns.SEEN)
    public boolean seen;
    
    @Mapping(value = Telephony.TextBasedSmsColumns.SUBSCRIPTION_ID)
    public long subscriptionId = -1;
    
    @Mapping(Telephony.TextBasedSmsColumns.PERSON)
    public long sender = -1;
    
    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", this._id);
        json.put("thread_id", this.threadId);
        json.put("telephony_id", this.subscriptionId);
        json.put("sender_id", this.sender);
        if (this.subject != null) json.put("subject", this.subject);
        json.put("text", this.body);
        if (this.receivedDate != null) json.put("received", this.receivedDate.getTime());
        if (this.sentDate != null) json.put("sent", this.sentDate.getTime());
        json.put("read", this.read);
        json.put("seen", this.seen);
        return json;
    }
}
