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
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

public class Actions {

    private static PowerManager.WakeLock wakeLock = null;
    private static int nextNotification = 0;

    public static void ping(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Receiver.logger.info("Ping received. nodecg-io-android is right here!");
        feedback.sendFeedback("pong");

        SensorManager mgr = ctx.getSystemService(SensorManager.class);
        System.out.println("Sensors: \n\n" + mgr.getSensorList(Sensor.TYPE_ALL).stream().map(Sensor::toString).collect(Collectors.joining("\n")));
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
    
    public static void checkAvailability(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        String type = data.getString("type");
        String value = data.getString("value");
        for (AvailableObject obj : AvailableObject.values()) {
            if (obj.type.equalsIgnoreCase(type) && obj.value.equalsIgnoreCase(value)) {
                feedback.sendFeedback("available", obj.available(ctx));
                return;
            }
        }
        feedback.sendFeedback("available", false);
    }
    
    public static void cancelSubscription(Context ctx, JSONObject data, Feedback feedback) throws JSONException {
        Subscription.cancel(ctx, UUID.fromString(data.getString("subscription_id")));
    }
    
    public static void cancelAllSubscriptions(Context ctx, JSONObject data, Feedback feedback) {
        Subscription.cancelAll(ctx, feedback.getPort());
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

    public static void showToast(Context ctx, JSONObject data, Feedback feedback) throws JSONException {
        Toast.makeText(ctx, data.getString("text"), Toast.LENGTH_LONG).show();
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
    
    public static void gpsActive(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Permissions.ensure(ctx, Permission.GPS);
        LocationManager mgr = ctx.getSystemService(LocationManager.class);
        feedback.sendFeedback("active", mgr.isProviderEnabled(LocationManager.GPS_PROVIDER));
    }
    
    public static void gpsLastKnownLocation(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Permissions.ensure(ctx, Permission.GPS);
        LocationManager mgr = ctx.getSystemService(LocationManager.class);
        if (!mgr.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            throw new FailureException("GPS is not active.");
        }
        Location lastKnown = mgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        JSONObject json = new JSONObject();
        if (lastKnown == null) {
            json.put("has_location", false);
        } else {
            json.put("has_location", true);
            json.put("location", Helper.locationToJson(lastKnown));
        }
        feedback.sendFeedback(json);
    }
    
    public static void gpsSubscribe(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Permissions.ensure(ctx, Permission.GPS);
        LocationManager mgr = ctx.getSystemService(LocationManager.class);
        int time = data.getInt("time");
        float distance = (float) data.getDouble("distance");
        Subscription subscription = Subscription.create(ctx, feedback);
        LocationListener listener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                try {
                    subscription.sendEvent(Helper.locationToJson(location));
                    Receiver.logger.info("Sending location update to subscriber: " + subscription.id);
                } catch (JSONException | FailureException e) {
                    Receiver.logger.warning("Failed to send location update: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }
            
            @Override
            @SuppressWarnings("deprecation")
            public void onStatusChanged(String provider, int status, Bundle extras) {
                //
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
                //
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
                //
            }
        };
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, listener);
        subscription.addCancellationHandler(context -> context.getSystemService(LocationManager.class).removeUpdates(listener));
    }
    
    public static void motionCurrent(Context ctx, JSONObject data, Feedback feedback) throws FailureException {
        SensorManager mgr = ctx.getSystemService(SensorManager.class);
        MotionSensors.sendMotionSensorFeedback(mgr, Feedback.delay(feedback));
    }

    public static void motionSubscribe(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        int time = data.getInt("time");
        int microseconds = time * 1000;
        int sensorId = Helper.getMotionSensorPart(data.getString("part"));
        SensorManager mgr = ctx.getSystemService(SensorManager.class);
        Sensor sensor = mgr.getDefaultSensor(sensorId);
        Subscription subscription = Subscription.create(ctx, feedback);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                try {
                    JSONObject json = new JSONObject();
                    MotionSensors.addSensorData(json, event, sensorId);
                    subscription.sendEvent(json);
                } catch (JSONException | FailureException e) {
                    Receiver.logger.warning("Failed to send motion sensor update: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                
            }
        };
        mgr.registerListener(listener, sensor, microseconds);
        subscription.addCancellationHandler(context -> context.getSystemService(SensorManager.class).unregisterListener(listener));
    }
}
