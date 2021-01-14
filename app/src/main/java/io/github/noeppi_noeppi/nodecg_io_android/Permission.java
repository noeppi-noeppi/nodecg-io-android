package io.github.noeppi_noeppi.nodecg_io_android;

import android.Manifest;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.*;
import static android.Manifest.permission.*;

public enum Permission {
    
    GPS("gps", ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, SDK_INT >= Q ?  ACCESS_BACKGROUND_LOCATION : null),
    PHONE("phone", READ_PHONE_STATE, READ_PHONE_NUMBERS),
    READ_SMS("read_sms", Manifest.permission.READ_SMS, RECEIVE_SMS, RECEIVE_MMS),
    SEND_SMS("send_sms", Manifest.permission.SEND_SMS),
    CONTACTS("contacts", READ_CONTACTS, GET_ACCOUNTS);
    
    // Id string used in nodecg-io-android
    public final String id;
    
    // Internal id string
    public final Set<String> perms;

    Permission(String id, String... perms) {
        this.id = id.toLowerCase();
        Set<String> set = new HashSet<>();
        for (String perm : perms) {
            if (perm != null) {
                set.add(perm);
            }
        }
        this.perms = ImmutableSet.copyOf(set);
    }
}
