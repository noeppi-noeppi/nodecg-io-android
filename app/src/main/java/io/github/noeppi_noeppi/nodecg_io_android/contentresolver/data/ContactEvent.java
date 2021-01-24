package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.ContactsContract;
import io.github.noeppi_noeppi.nodecg_io_android.Helper;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;

@DataClass
@SuppressWarnings("CanBeFinal")
public class ContactEvent implements ToJSON {

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Event.START_DATE)
    public String date;

    @Mapping(ContactsContract.CommonDataKinds.Event.TYPE)
    public int type;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Event.LABEL)
    public String type_label;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.date == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("date", this.date);
        json.put("type", Helper.getContactEventType(this.type));
        if (this.type_label != null) {
            json.put("type_label", this.type_label);
        }
        return json;
    }
}
