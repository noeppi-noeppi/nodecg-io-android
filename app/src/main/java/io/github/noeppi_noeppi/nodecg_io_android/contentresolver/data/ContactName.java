package io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data;

import android.provider.BaseColumns;
import android.provider.ContactsContract;
import io.github.noeppi_noeppi.nodecg_io_android.Helper;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.DataClass;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.mapping.Mapping;
import io.github.noeppi_noeppi.nodecg_io_android.util.ToJSON;
import org.json.JSONException;
import org.json.JSONObject;

@DataClass
@SuppressWarnings("CanBeFinal")
public class ContactName implements ToJSON {

    @Mapping(BaseColumns._ID)
    public long _id;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME)
    public String displayName;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME)
    public String givenName;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME)
    public String familiyName;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.PREFIX)
    public String prefix;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.MIDDLE_NAME)
    public String middleName;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.SUFFIX)
    public String suffix;

    @Mapping(ContactsContract.CommonDataKinds.StructuredName.FULL_NAME_STYLE)
    public int nameStyle;

    public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("display_name", this.displayName == null ? "" : this.displayName);
        if (this.givenName != null) json.put("given_name", this.givenName);
        if (this.familiyName != null) json.put("family_name", this.familiyName);
        if (this.prefix != null) json.put("prefix", this.prefix);
        if (this.middleName != null) json.put("middle_name", this.middleName);
        if (this.suffix != null) json.put("suffix", this.suffix);
        json.put("style", Helper.getContactNameStyle(this.nameStyle));
        return json;
    }
}
