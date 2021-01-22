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
public class ContactNickname implements ToJSON {

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Nickname.NAME)
    public String name;
    
    @Mapping(ContactsContract.CommonDataKinds.Nickname.TYPE)
    public int type;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Nickname.LABEL)
    public String type_label;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.name == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("name", this.name);
        json.put("type", Helper.getContactNicknameType(this.type));
        if (this.type_label != null) {
            json.put("type_label", this.type_label);
        }
        return json;
    }
}
