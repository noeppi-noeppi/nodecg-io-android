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
public class ContactPhone implements ToJSON {

    @Mapping(ContactsContract.CommonDataKinds.Phone.NUMBER)
    public String enteredNumber;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Phone.NORMALIZED_NUMBER)
    public String number;

    @Mapping(ContactsContract.CommonDataKinds.Phone.TYPE)
    public int type;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Phone.LABEL)
    public String type_label;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.number == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("entered_number", this.enteredNumber);
        json.put("number", this.number);
        json.put("type", Helper.getPhoneNumberType(this.type));
        if (this.type_label != null) {
            json.put("type_label", this.type_label);
        }
        return json;
    }
}
