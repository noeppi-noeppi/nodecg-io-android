package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Icon;
import android.media.AudioManager;
import android.os.PowerManager;
import android.util.Base64;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.List;

public class Actions {

    private static PowerManager.WakeLock wakeLock = null;
    private static int nextNotification = 0;

    public static void ping(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Receiver.logger.info("Ping received. nodecg-io-android is right here!");
        feedback.sendFeedback("pong");
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
