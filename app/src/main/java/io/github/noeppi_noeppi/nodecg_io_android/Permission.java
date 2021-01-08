package io.github.noeppi_noeppi.nodecg_io_android;

import android.Manifest;
import android.os.Build;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;

public enum Permission {
    
    GPS("gps", Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ?  Manifest.permission.ACCESS_BACKGROUND_LOCATION : null)
    ;
    
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
