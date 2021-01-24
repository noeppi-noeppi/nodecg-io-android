package io.github.noeppi_noeppi.nodecg_io_android;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import com.google.common.collect.ImmutableSet;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import static android.Manifest.permission.*;
import static android.os.Build.VERSION.SDK_INT;
import static android.os.Build.VERSION_CODES.Q;

public enum Permission {

    GPS("gps", null, ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION, SDK_INT >= Q ? ACCESS_BACKGROUND_LOCATION : null),
    PHONE("phone", null, READ_PHONE_STATE, READ_PHONE_NUMBERS),
    READ_SMS("read_sms", null, Manifest.permission.READ_SMS, RECEIVE_SMS, RECEIVE_MMS),
    SEND_SMS("send_sms", null, Manifest.permission.SEND_SMS),
    CONTACTS("contacts", null, READ_CONTACTS, GET_ACCOUNTS),
    STATISTICS("statistics", ctx -> {
        //AppOpsManager mgr = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
        AppOpsManager mgr = ctx.getSystemService(AppOpsManager.class);
        //noinspection deprecation
        int mode = mgr.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), ctx.getPackageName());
        if (mode == AppOpsManager.MODE_DEFAULT) {
            return ctx.checkCallingOrSelfPermission(android.Manifest.permission.PACKAGE_USAGE_STATS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return mode == AppOpsManager.MODE_ALLOWED;
        }
    }, PACKAGE_USAGE_STATS);

    // Id string used in nodecg-io-android
    public final String id;

    // Whether this is a special permission that requires a redirect to the settings page.
    // Those permissions are not normally requestable bt must be requested one after
    // another with a special call
    // Null means normal runtime permission. Nonnull means a function with custom permission check.
    public final Function<Context, Boolean> special;

    // Internal id string
    public final Set<String> perms;

    Permission(String id, Function<Context, Boolean> special, String... perms) {
        this.id = id.toLowerCase();
        this.special = special;
        Set<String> set = new HashSet<>();
        for (String perm : perms) {
            if (perm != null) {
                set.add(perm);
            }
        }
        this.perms = ImmutableSet.copyOf(set);
    }
}
