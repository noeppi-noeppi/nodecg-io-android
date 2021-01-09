package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import android.location.LocationManager;
import org.json.JSONException;

import java.util.List;

public enum AvailableObject {
    
    GPS("sensor", "gps", ctx -> {
        LocationManager mgr = ctx.getSystemService(LocationManager.class);
        if (mgr != null) {
            List<String> providers = mgr.getAllProviders();
            return providers != null && providers.contains(LocationManager.GPS_PROVIDER);
        }
        return false;
    }, Permission.GPS)
    ;
    
    public final String type;
    public final String value;
    private final FailFunction<Context, Boolean> available;
    private final Permission[] permissions;
    
    AvailableObject(String type, String value, FailFunction<Context, Boolean> available, Permission... permissions) {
        this.type = type;
        this.value = value;
        this.available = available;
        this.permissions = permissions;
    }
    
    public boolean available(Context ctx) throws JSONException, FailureException {
        Permissions.ensure(ctx, this.permissions);
        return this.available.apply(ctx);
    }
}
