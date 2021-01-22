package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.BaseColumns;
import android.provider.Telephony;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import java.util.Date;

@DataClass
public class Mms implements ToJSON {

    @Mapping(BaseColumns._ID)
    public long _id;

    @Nullable
    @Mapping(Telephony.BaseMmsColumns.SUBJECT)
    public String subject;

    @Nullable
    @Mapping(value = Telephony.BaseMmsColumns.DATE)
    public Date receivedDate;

    @Nullable
    @Mapping(value = Telephony.BaseMmsColumns.DATE_SENT)
    public Date sentDate;

    @Mapping(Telephony.BaseMmsColumns.THREAD_ID)
    public long threadId = -1;
    
    @Mapping(Telephony.BaseMmsColumns.READ)
    public boolean read;
    
    @Mapping(Telephony.BaseMmsColumns.SEEN)
    public boolean seen;

    @Mapping(value = Telephony.BaseMmsColumns.SUBSCRIPTION_ID)
    public long subscriptionId = -1;

    @Mapping(Telephony.BaseMmsColumns.TEXT_ONLY)
    public boolean textOnly;

    @Nullable
    @Mapping(Telephony.BaseMmsColumns.RETRIEVE_TEXT)
    public String retrieveText;
    
    @Mapping(Telephony.BaseMmsColumns.CONTENT_TYPE)
    public String contentType = "unknown";
    
    @Nullable
    @Mapping(Telephony.BaseMmsColumns.CONTENT_LOCATION)
    public String contentLocation;
    
    @Nullable
    @Mapping(Telephony.BaseMmsColumns.EXPIRY)
    public Date expiry;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", this._id);
        json.put("thread_id", this.threadId);
        json.put("telephony_id", this.subscriptionId);
        if (this.subject != null) json.put("subject", this.subject);
        if (this.retrieveText != null) json.put("text", this.retrieveText);
        if (this.receivedDate != null) json.put("received", this.receivedDate.getTime());
        if (this.sentDate != null) json.put("sent", this.sentDate.getTime());
        json.put("read", this.read);
        json.put("seen", this.seen);
        json.put("textOnly", this.textOnly);
        json.put("contentType", this.contentType);
        if (this.contentLocation != null) json.put("contentLocation", this.contentLocation);
        if (this.expiry != null) json.put("expiry", this.expiry);
        return json;
    }
}
