package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;

import java.util.HashSet;
import java.util.Set;

public class Permissions {

    public static void ensure(Context ctx, Permission... permissions) throws FailureException {
        Set<String> noPerm = new HashSet<>();
        for (Permission permission : permissions) {
            if (permission.special != null) {
                if (!permission.special.apply(ctx)) {
                    noPerm.add(permission.id);
                }
            } else {
                for (String perm : permission.perms) {
                    if (ContextCompat.checkSelfPermission(ctx, perm) != PackageManager.PERMISSION_GRANTED) {
                        noPerm.add(permission.id);
                    }
                }
            }
        }
        if (!noPerm.isEmpty()) {
            throw new FailureException("Permissions required for that operation are not granted. Request all you permissions at bundle start with a single call to requestPermissions(). Permissions not granted: " + String.join(", ", noPerm));
        }
    }
}
