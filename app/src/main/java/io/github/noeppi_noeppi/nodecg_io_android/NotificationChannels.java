package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NotificationChannels {

    private static final Map<Properties, NotificationChannel> channels = new HashMap<>();

    public static NotificationChannel get(Context ctx, JSONObject msg) throws JSONException {
        Properties properties = getProperties(msg);
        if (channels.containsKey(properties)) {
            return channels.get(properties);
        } else {
            NotificationChannel nc = new NotificationChannel("nodecg-io-" + properties.toString(), "nodecg-io", properties.importance);
            nc.setLockscreenVisibility(properties.visibleOnLockScreen);
            nc.setBypassDnd(properties.bypassDnd);

            NotificationManager nm = ctx.getSystemService(NotificationManager.class);
            nm.createNotificationChannel(nc);
            channels.put(properties, nc);
            return nc;
        }
    }

    public static Properties getProperties(JSONObject msg) throws JSONException {
        JSONObject properties = msg.getJSONObject("properties");
        String importanceStr = properties.optString("importance", "default").toLowerCase();
        int importance;
        switch (importanceStr) {
            case "high":
                importance = NotificationManager.IMPORTANCE_HIGH;
                break;
            case "low":
                importance = NotificationManager.IMPORTANCE_LOW;
                break;
            case "min":
                importance = NotificationManager.IMPORTANCE_MIN;
                break;
            case "max":
                importance = NotificationManager.IMPORTANCE_MAX;
                break;
            case "default":
            default:
                importance = NotificationManager.IMPORTANCE_DEFAULT;
                break;
        }

        String modeStr = properties.optString("mode", "private").toLowerCase();
        int mode;
        switch (modeStr) {
            case "public":
                mode = Notification.VISIBILITY_PUBLIC;
                break;
            case "secret":
                mode = Notification.VISIBILITY_SECRET;
                break;
            case "private":
            default:
                mode = Notification.VISIBILITY_PRIVATE;
                break;
        }

        boolean bypassDnd = properties.optBoolean("bypass_dnd", false);
        return new Properties(importance, mode, bypassDnd);
    }

    public static class Properties {

        public final int importance;
        public final int visibleOnLockScreen;
        public final boolean bypassDnd;

        public Properties(int importance, int visibleOnLockScreen, boolean bypassDnd) {
            this.importance = importance;
            this.visibleOnLockScreen = visibleOnLockScreen;
            this.bypassDnd = bypassDnd;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || this.getClass() != o.getClass()) return false;
            Properties that = (Properties) o;
            return this.importance == that.importance &&
                    this.visibleOnLockScreen == that.visibleOnLockScreen &&
                    this.bypassDnd == that.bypassDnd;
        }

        @Override
        public int hashCode() {
            return Objects.hash(this.importance, this.visibleOnLockScreen, this.bypassDnd);
        }

        @Override
        @SuppressWarnings("NullableProblems")
        public String toString() {
            return this.importance + "-" + this.visibleOnLockScreen + "-" + this.bypassDnd;
        }
    }
}
