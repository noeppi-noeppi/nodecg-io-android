package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.BaseColumns;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.json.JSONException;
import org.json.JSONObject;

@DataClass
@SuppressWarnings("CanBeFinal")
public class Recipient implements ToJSON {

    @Mapping(BaseColumns._ID)
    public long _id;

    // Might be a phone number or a name.
    @Mapping("address")
    public String address;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("id", this._id);
        json.put("address", this.address);
        return json;
    }
}
