package io.github.noeppi_noeppi.nodecg_io_android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.AppliedFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentType;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Mms;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Sms;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;

public class Helper {

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
    
    public static Set<Integer> getTelephonyIds(Context ctx) throws FailureException {
        Permissions.ensure(ctx, Permission.PHONE);
        SubscriptionManager subm = ctx.getSystemService(SubscriptionManager.class);
        Set<Integer> ids = new HashSet<>();
        @SuppressLint("MissingPermission")
        List<SubscriptionInfo> subList = subm.getActiveSubscriptionInfoList();
        if (subList != null) {
            for (SubscriptionInfo subInfo : subList) {
                ids.add(subInfo.getSubscriptionId());
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            subList = subm.getAccessibleSubscriptionInfoList();
            if (subList != null) {
                for (SubscriptionInfo subInfo : subm.getAccessibleSubscriptionInfoList()) {
                    ids.add(subInfo.getSubscriptionId());
                }
            }
        }
        ids.remove(SubscriptionManager.INVALID_SUBSCRIPTION_ID);
        return ids;
    }
    
    public static SubscriptionInfo getTelephony(Context ctx, JSONObject data) throws JSONException, FailureException {
        Permissions.ensure(ctx, Permission.PHONE);
        SubscriptionManager subm = ctx.getSystemService(SubscriptionManager.class);
        int subscriptionId = data.getInt("telephony");
        if (subscriptionId == SubscriptionManager.INVALID_SUBSCRIPTION_ID) {
            throw new FailureException("Tries to access INVALID_SUBSCRIPTION_ID");
        }
        @SuppressLint("MissingPermission")
        SubscriptionInfo subInfo = subm.getActiveSubscriptionInfo(subscriptionId);
        if (subInfo == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            List<SubscriptionInfo> subList = subm.getAccessibleSubscriptionInfoList();
            if (subList != null) {
                subInfo = subm.getAccessibleSubscriptionInfoList().stream().filter(s -> s.getSubscriptionId() == subscriptionId).findFirst().orElse(null);
            }
        }
        if (subInfo == null) {
            throw new FailureException("Telephony not found: " + subscriptionId);
        }
        return subInfo;
    }
    
    public static TelephonyManager getTelephonyManager(Context ctx, SubscriptionInfo subInfo) throws FailureException {
        TelephonyManager mgr = ctx.getSystemService(TelephonyManager.class).createForSubscriptionId(subInfo.getSubscriptionId());
        if (mgr == null) {
            throw new FailureException("Could not access telephony: " + subInfo.getSubscriptionId());
        }
        return mgr;
    }
    
    public static SmsManager getSmsManager(Context ctx, SubscriptionInfo subInfo) throws FailureException {
        SmsManager mgr = SmsManager.getSmsManagerForSubscriptionId(subInfo.getSubscriptionId());
        if (mgr == null) {
            throw new FailureException("Could not access sms: " + subInfo.getSubscriptionId());
        }
        return mgr;
    }
    
    public static AppliedFilter<?> getSMSFilter(Context ctx, JSONObject data) throws JSONException, FailureException {
        String smsFilter = data.getString("sms_filter");
        JSONObject resolveData = data.getJSONObject("sms_resolve_data");
        switch (smsFilter.toLowerCase()) {
            case "everything":
                return ContentFilter.EVERYTHING.apply(null);
            case "telephony":
                return ContentFilter.SUBSCRIPTION.apply(getTelephony(ctx, resolveData));
            case "thread":
                return ContentFilter.THREAD.apply(resolveData.getLong("thread_id"));
            default:
                throw new FailureException("Unknown SMS filter: " + smsFilter);
        }
    }
    
    public static ContentType<Sms> getSmsType(JSONObject data) throws JSONException, FailureException {
        String smsCategory = data.getString("sms_category");
        switch (smsCategory.toLowerCase()) {
            case "all": return ContentType.SMS_ALL;
            case "inbox": return ContentType.SMS_INBOX;
            case "outbox": return ContentType.SMS_OUTBOX;
            case "sent": return ContentType.SMS_SENT;
            case "draft": return ContentType.SMS_DRAFT;
            default: throw new FailureException("Unknown SMS category: " + smsCategory);
        }
    }

    public static  ContentType<Mms> getMmsType(JSONObject data) throws JSONException, FailureException {
        String smsCategory = data.getString("sms_category");
        switch (smsCategory.toLowerCase()) {
            case "all": return ContentType.MMS_ALL;
            case "inbox": return ContentType.MMS_INBOX;
            case "outbox": return ContentType.MMS_OUTBOX;
            case "sent": return ContentType.MMS_SENT;
            case "draft": return ContentType.MMS_DRAFT;
            default: throw new FailureException("Unknown SMS category: " + smsCategory);
        }
    }
}
