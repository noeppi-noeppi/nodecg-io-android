package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.ContactsContract;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;

@DataClass
@SuppressWarnings("CanBeFinal")
public class ContactNotes implements ToJSON {

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.Note.NOTE)
    public String text;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.text == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("text", this.text);
        return json;
    }
}
