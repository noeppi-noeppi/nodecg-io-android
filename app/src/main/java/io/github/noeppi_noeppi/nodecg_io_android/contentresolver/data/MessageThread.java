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
public class MessageThread implements ToJSON {

    @Mapping(BaseColumns._ID)
    public long _id;
    
    @Nullable
    @Mapping(Telephony.ThreadsColumns.DATE)
    public Date creationDate;
    
    @Nullable
    @Mapping(Telephony.ThreadsColumns.RECIPIENT_IDS)
    public String recipients;
    
    @Mapping(Telephony.ThreadsColumns.MESSAGE_COUNT)
    public int messageCount;
    
    @Mapping(Telephony.ThreadsColumns.READ)
    public boolean allRead;
    
    @Mapping(Telephony.ThreadsColumns.SNIPPET)
    public String lastMessageSnippet;
    
    @Mapping(Telephony.ThreadsColumns.TYPE)
    public int threadType;
    
    @Mapping(Telephony.ThreadsColumns.ARCHIVED)
    public boolean archived;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", this._id);
        if (this.creationDate != null) json.put("creationDat", this.creationDate.getTime());
        json.put("messageCount", this.messageCount);
        json.put("allRead", this.allRead);
        if (this.lastMessageSnippet != null) json.put("snippet", this.lastMessageSnippet);
        json.put("broadcast", this.threadType == Telephony.Threads.BROADCAST_THREAD);
        json.put("archived", this.archived);
        return json;
    }
}
