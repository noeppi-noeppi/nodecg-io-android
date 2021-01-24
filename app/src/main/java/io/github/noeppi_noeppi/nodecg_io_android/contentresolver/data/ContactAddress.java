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
public class ContactAddress implements ToJSON {

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)
    public String address;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.STREET)
    public String street;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.POBOX)
    public String postBox;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.NEIGHBORHOOD)
    public String neighbourhood;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.CITY)
    public String city;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.REGION)
    public String region;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE)
    public String postCode;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY)
    public String country;

    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.TYPE)
    public int type;

    @Nullable
    @Mapping(ContactsContract.CommonDataKinds.StructuredPostal.LABEL)
    public String type_label;

    @Nullable
    @Override
    public JSONObject toJSON() throws JSONException {
        if (this.address == null) {
            return null;
        }
        JSONObject json = new JSONObject();
        json.put("address", this.address);
        if (this.street != null) json.put("street", this.street);
        if (this.postBox != null) json.put("post_box", this.postBox);
        if (this.neighbourhood != null) json.put("neighbourhood", this.neighbourhood);
        if (this.city != null) json.put("city", this.city);
        if (this.region != null) json.put("region", this.region);
        if (this.postCode != null) json.put("post_code", this.postCode);
        if (this.country != null) json.put("country", this.country);
        json.put("type", Helper.getContactAddressType(this.type));
        if (this.type_label != null) {
            json.put("type_label", this.type_label);
        }
        return json;
    }
}
