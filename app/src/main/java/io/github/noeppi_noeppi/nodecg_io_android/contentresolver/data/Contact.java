package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.BaseColumns;
import android.provider.ContactsContract;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSONArray;
import org.json.JSONArray;

@DataClass
@SuppressWarnings("CanBeFinal")
public class Contact implements ToJSONArray {

    @Mapping(BaseColumns._ID)
    public long _id;

    @Mapping(ContactsContract.Contacts.DISPLAY_NAME)
    public String displayName;

    @Mapping(ContactsContract.Contacts.NAME_RAW_CONTACT_ID)
    public long nameInfoId;

    public JSONArray toJSON() {
        JSONArray array = new JSONArray();
        array.put(this._id);
        array.put(this.displayName);
        return array;
    }
}
