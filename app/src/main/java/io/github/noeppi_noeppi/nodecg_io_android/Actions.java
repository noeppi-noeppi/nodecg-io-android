package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class Actions {

    private static PowerManager.WakeLock wakeLock = null;
    private static int nextNotification = 0;

    public static void ping(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Receiver.logger.info("Ping received. nodecg-io-android is right here!");
        feedback.sendFeedback("pong");
    }
    
    public static void requestPermissions(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        JSONArray permissionsArray = data.getJSONArray("permissions");
        Set<String> permissions = new HashSet<>();
        for (int i = 0; i < permissionsArray.length(); i++) {
            String permissionStr = permissionsArray.getString(i).toLowerCase();
            for (Permission permission : Permission.values()) {
                if (permission.id.equals(permissionStr)) {
                    permissions.addAll(permission.perms);
                }
            }
        }
        permissions = permissions.stream().filter(perm -> ContextCompat.checkSelfPermission(ctx, perm) != PackageManager.PERMISSION_GRANTED).collect(Collectors.toSet());
        if (permissions.isEmpty()) {
            feedback.sendFeedback("success", true);
            Receiver.logger.info("Requesting no permissions: all granted.");
        } else if (!Settings.canDrawOverlays(ctx)) {
            JSONObject json = new JSONObject();
            json.put("success", false);
            json.put("errmsg", "nodecg-io-android has no SYSTEM ALERT WINDOW permission. STart the app and you'll be redirected to the settings page to grant that permission.");
            Receiver.logger.info("Failed to request runtime permissions: SYSTEM ALERT WINDOW permission not granted.");
        } else {
            Intent intent = new Intent(ctx, MainActivity.class);
            intent.putExtra("io.github.noeppi_noeppi.nodecg_io_android.REQUEST_PERMISSIONS", permissions.toArray(new String[]{}));
            Feedback.attach(intent, feedback);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            
            // Very weird but it seems to only work this way...
            PendingIntent pi = PendingIntent.getActivity(ctx, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
            try {
                pi.send(ctx, 0, intent);
                Receiver.logger.info("Requesting permissions: " + String.join(", ", permissions));
            } catch (PendingIntent.CanceledException e) {
                Receiver.logger.warning("Could not launch activity: " + e.getMessage());
                e.printStackTrace();
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("errmsg", "Could not launch activity: " + e.getMessage());
                feedback.sendFeedback(json);
            }
        }
    }
    
    public static void getVolume(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        AudioManager audio = ctx.getSystemService(AudioManager.class);
        feedback.sendFeedback("volume", audio.getStreamVolume(Helper.getAudioStream(data)));
    }

    public static void getMaxVolume(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        AudioManager audio = ctx.getSystemService(AudioManager.class);
        feedback.sendFeedback("volume", audio.getStreamMaxVolume(Helper.getAudioStream(data)));
    }

    public static void setVolume(Context ctx, JSONObject data, Feedback feedback) throws JSONException {
        AudioManager audio = ctx.getSystemService(AudioManager.class);
        audio.setStreamVolume(Helper.getAudioStream(data), data.getInt("volume"), Helper.getAudioFlags(data));
    }

    public static void adjustVolume(Context ctx, JSONObject data, Feedback feedback) throws JSONException {
        AudioManager audio = ctx.getSystemService(AudioManager.class);
        audio.adjustStreamVolume(Helper.getAudioStream(data), Helper.getAudioAdjustment(data), Helper.getAudioFlags(data));
    }

    public static void wakeUp(Context ctx, JSONObject data, Feedback feedback) {
        PowerManager power = ctx.getSystemService(PowerManager.class);
        if (wakeLock == null) {
            //noinspection deprecation
            wakeLock = power.newWakeLock(PowerManager.FULL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "nodecg-io:wakelock");
        }
        wakeLock.acquire(1000);
        wakeLock.release();
    }

    public static void getPackages(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        List<PackageInfo> packages = ctx.getPackageManager().getInstalledPackages(0);
        JSONArray array = new JSONArray();
        packages.forEach(p -> array.put(p.packageName));
        feedback.sendFeedback("packages", array);
    }

    public static void getPackage(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Helper.getPackage(ctx, data);
    }

    public static void getActivities(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        ActivityInfo[] activities = Helper.getPackage(ctx, data).activities;
        JSONArray array = new JSONArray();
        Arrays.stream(activities).forEach(p -> array.put(p.name));
        feedback.sendFeedback("activities", array);
    }

    public static void getActivity(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Helper.getActivity(ctx, data);
    }

    public static void startActivity(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        ActivityInfo activity = Helper.getActivity(ctx, data);
        Intent intent = new Intent().setAction(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER).setClassName(activity.packageName, activity.name);
        ctx.startActivity(intent);
    }

    public static void getPackageVersion(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        PackageInfo pkg = Helper.getPackage(ctx, data);
        feedback.sendFeedback("version", pkg.versionName);
    }

    public static void notify(Context ctx, JSONObject data, Feedback feedback) throws JSONException {
        Icon icon;
        if (data.has("icon") && data.get("icon") != JSONObject.NULL) {
            byte[] imageData = Base64.decode(data.getString("icon"), Base64.DEFAULT);
            icon = Icon.createWithData(imageData, 0, imageData.length);
        } else {
            icon = Icon.createWithResource(ctx, R.drawable.logo);
        }
        NotificationManager nm = ctx.getSystemService(NotificationManager.class);
        NotificationChannel nc = NotificationChannels.get(ctx, data);

        JSONObject properties = data.getJSONObject("properties");
        boolean autoHide = properties.optBoolean("auto_hide", true);

        Notification nn = new Notification.Builder(ctx, nc.getId()).setVisibility(nc.getLockscreenVisibility())
                .setContentIntent(feedback.getEvent(ctx, new JSONObject()))
                .setSmallIcon(icon).setSmallIcon(icon)
                .setContentTitle(data.getString("title"))
                .setContentText(data.getString("text"))
                .setAutoCancel(autoHide)
                .build();
        nm.notify(nextNotification++, nn);
    }
}
