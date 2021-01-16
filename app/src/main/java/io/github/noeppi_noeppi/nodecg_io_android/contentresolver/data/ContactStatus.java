package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.ContactsContract;
import io.github.noeppi_noeppi.nodecg_io_android.Helper;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@DataClass
public class ContactStatus {

    @Mapping(ContactsContract.Contacts.CONTACT_PRESENCE)
    public int presenceId = ContactsContract.StatusUpdates.OFFLINE;

    @Mapping(ContactsContract.Contacts.CONTACT_STATUS)
    public String status;

    @Mapping(ContactsContract.Contacts.CONTACT_STATUS_TIMESTAMP)
    public Date statusTime;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("presence", Helper.getContactPresence(this.presenceId));
        if (this.status != null) json.put("status", this.status);
        if (this.statusTime != null) json.put("status_time", this.statusTime.getTime());
        return json;
    }
}
