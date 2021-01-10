package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.*;
import android.location.Location;
import android.media.AudioManager;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class Helper {

    private static NotificationChannel notificationsLow = null;
    private static NotificationChannel notificationsNormal = null;
    private static NotificationChannel notificationsHigh = null;

    public static int getAudioStream(JSONObject msg) throws JSONException {
        String channel = msg.getString("channel").toLowerCase();
        switch (channel) {
            case "accessibility":
                return AudioManager.STREAM_ACCESSIBILITY;
            case "alarm":
                return AudioManager.STREAM_ALARM;
            case "dtmf":
                return AudioManager.STREAM_DTMF;
            case "music":
                return AudioManager.STREAM_MUSIC;
            case "notification":
                return AudioManager.STREAM_NOTIFICATION;
            case "system":
                return AudioManager.STREAM_SYSTEM;
            case "voice_call":
                return AudioManager.STREAM_VOICE_CALL;
            case "ring":
            default:
                return AudioManager.STREAM_RING;
        }
    }

    public static int getAudioFlags(JSONObject msg) throws JSONException {
        int flags = 0;
        JSONArray flagList = msg.getJSONArray("flags");
        for (int i = 0; i < flagList.length(); i++) {
            String flag = flagList.getString(i);
            switch (flag.toLowerCase()) {
                case "show_ui":
                    flags |= AudioManager.FLAG_SHOW_UI;
                    break;
                case "play_sound":
                    flags |= AudioManager.FLAG_PLAY_SOUND;
                    break;
                case "ringer_modes":
                    flags |= AudioManager.FLAG_ALLOW_RINGER_MODES;
                    break;
                case "silent":
                    flags |= AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE;
                    break;
                case "vibrate":
                    flags |= AudioManager.FLAG_VIBRATE;
                    break;
            }
        }
        return flags;
    }

    public static int getAudioAdjustment(JSONObject msg) throws JSONException {
        String adjustment = msg.getString("adjustment").toLowerCase();
        switch (adjustment) {
            case "raise":
                return AudioManager.ADJUST_RAISE;
            case "lower":
                return AudioManager.ADJUST_LOWER;
            case "mute":
                return AudioManager.ADJUST_MUTE;
            case "unmute":
                return AudioManager.ADJUST_UNMUTE;
            case "toggle_mute":
                return AudioManager.ADJUST_TOGGLE_MUTE;
            case "same":
            default:
                return AudioManager.ADJUST_SAME;
        }
    }

    public static PackageInfo getPackage(Context ctx, JSONObject msg) throws JSONException, FailureException {
        String pname = msg.getString("package");
        try {
            return ctx.getPackageManager().getPackageInfo(pname, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new FailureException("Package '" + pname + "' not found.", e);
        }
    }

    public static ActivityInfo getActivity(Context ctx, JSONObject msg) throws JSONException, FailureException {
        PackageInfo pkg = getPackage(ctx, msg);
        String aname = msg.getString("activity");
        Optional<ActivityInfo> info = Arrays.stream(pkg.activities).filter(a -> a.name.equals(aname)).findFirst();
        if (info.isPresent()) {
            return info.get();
        }  else {
            throw new FailureException("Activity '" + aname + "' not found in package '" + pkg.packageName + "'.");
        }
    }
    
    public static JSONObject locationToJson(Location location) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("latitude", location.getLatitude());
        json.put("longitude", location.getLongitude());
        if (location.hasAltitude()) {
            json.put("altitude", location.getAltitude());
        }
        if (location.hasSpeed()) {
            json.put("speed", location.getSpeed());
        }
        if (location.hasBearing()) {
            json.put("bearing", location.getBearing());
        }
        if (location.hasAccuracy()) {
            json.put("accuracyHorizontal", location.getAccuracy());
        }
        if (location.hasVerticalAccuracy()) {
            json.put("accuracyVertical", location.getVerticalAccuracyMeters());
        }
        if (location.hasSpeedAccuracy()) {
            json.put("accuracySpeed", location.getSpeedAccuracyMetersPerSecond());
        }
        if (location.hasBearingAccuracy()) {
            json.put("accuracyBearing", location.getBearingAccuracyDegrees());
        }
        return json;
    }
    
    public static int getMotionSensorPart(String part) throws FailureException {
        switch (part) {
            case "accelerometer": return Sensor.TYPE_ACCELEROMETER;
            case "accelerometer_uncalibrated": return Sensor.TYPE_ACCELEROMETER_UNCALIBRATED;
            case "gravity": return Sensor.TYPE_GRAVITY;
            case "gyroscope": return Sensor.TYPE_GYROSCOPE;
            case "gyroscope_uncalibrated": return Sensor.TYPE_GYROSCOPE_UNCALIBRATED;
            case "linear_acceleration": return Sensor.TYPE_LINEAR_ACCELERATION;
            case "rotation_vector": return Sensor.TYPE_ROTATION_VECTOR;
            default: throw new FailureException("Unknown motion sensor part: " + part);
        }
    }
}
