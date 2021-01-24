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
public class ContactEmail implements ToJSON {

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Email.ADDRESS)
    public String address;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Email.DISPLAY_NAME)
    public String display_name;

    @Mapping(ContactsContract.CommonDataKinds.Email.TYPE)
    public int type;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Email.LABEL)
    public String type_label;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.address == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("address", this.address);
        json.put("display_name", this.display_name == null ? this.address : this.display_name);
        json.put("type", Helper.getContactEmailType(this.type));
        if (this.type_label != null) {
            json.put("type_label", this.type_label);
        }
        return json;
    }
}
