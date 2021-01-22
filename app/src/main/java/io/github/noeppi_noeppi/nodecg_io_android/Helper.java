package io.github.noeppi_noeppi.nodecg_io_android;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.location.Location;
import android.media.AudioManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.ContactsContract;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.AppliedFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentFilter;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.ContentType;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Mms;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.Sms;
import org.apache.commons.lang3.tuple.Pair;
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
        } else {
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

    public static ContentType<Mms> getMmsType(JSONObject data) throws JSONException, FailureException {
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

    public static String getSmsResult(int code) {
        switch (code) {
            case Activity.RESULT_OK: return "success";
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE: return "error_generic_failure";
            case SmsManager.RESULT_ERROR_RADIO_OFF: return "error_radio_off";
            case SmsManager.RESULT_ERROR_NULL_PDU: return "error_null_pdu";
            case SmsManager.RESULT_ERROR_NO_SERVICE: return "error_no_service";
            case SmsManager.RESULT_ERROR_LIMIT_EXCEEDED: return "error_limit_exceeded";
            case SmsManager.RESULT_ERROR_FDN_CHECK_FAILURE: return "error_fdn_check_failure";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NOT_ALLOWED: return "error_short_code_not_allowed";
            case SmsManager.RESULT_ERROR_SHORT_CODE_NEVER_ALLOWED: return "error_short_code_never_allowed";
            case SmsManager.RESULT_RADIO_NOT_AVAILABLE: return "radio_not_available";
            case SmsManager.RESULT_NETWORK_REJECT: return "network_reject";
            case SmsManager.RESULT_INVALID_ARGUMENTS: return "invalid_arguments";
            case SmsManager.RESULT_INVALID_STATE: return "invalid_state";
            case SmsManager.RESULT_NO_MEMORY: return "no_memory";
            case SmsManager.RESULT_INVALID_SMS_FORMAT: return "invalid_sms_format";
            case SmsManager.RESULT_SYSTEM_ERROR: return "system_error";
            case SmsManager.RESULT_MODEM_ERROR: return "modem_error";
            case SmsManager.RESULT_NETWORK_ERROR: return "network_error";
            case SmsManager.RESULT_ENCODING_ERROR: return "encoding_error";
            case SmsManager.RESULT_INVALID_SMSC_ADDRESS: return "invalid_smsc_address";
            case SmsManager.RESULT_OPERATION_NOT_ALLOWED: return "operation_not_allowed";
            case SmsManager.RESULT_INTERNAL_ERROR: return "internal_error";
            case SmsManager.RESULT_NO_RESOURCES: return "no_resources";
            case SmsManager.RESULT_CANCELLED: return "cancelled";
            case SmsManager.RESULT_REQUEST_NOT_SUPPORTED: return "request_not_supported";
            case SmsManager.RESULT_NO_BLUETOOTH_SERVICE: return "no_bluetooth_service";
            case SmsManager.RESULT_INVALID_BLUETOOTH_ADDRESS: return "invalid_bluetooth_address";
            case SmsManager.RESULT_BLUETOOTH_DISCONNECTED: return "bluetooth_disconnected";
            case SmsManager.RESULT_UNEXPECTED_EVENT_STOP_SENDING: return "unexpected_event_stop_sending";
            case SmsManager.RESULT_SMS_BLOCKED_DURING_EMERGENCY: return "sms_blocked_during_emergency";
            case SmsManager.RESULT_SMS_SEND_RETRY_FAILED: return "sms_send_retry_failed";
            case SmsManager.RESULT_REMOTE_EXCEPTION: return "remote_exception";
            case SmsManager.RESULT_NO_DEFAULT_SMS_APP: return "no_default_sms_app";
            case SmsManager.RESULT_RIL_RADIO_NOT_AVAILABLE: return "ril_radio_not_available";
            case SmsManager.RESULT_RIL_SMS_SEND_FAIL_RETRY: return "ril_sms_send_fail_retry";
            case SmsManager.RESULT_RIL_NETWORK_REJECT: return "ril_network_reject";
            case SmsManager.RESULT_RIL_INVALID_STATE: return "ril_invalid_state";
            case SmsManager.RESULT_RIL_INVALID_ARGUMENTS: return "ril_invalid_arguments";
            case SmsManager.RESULT_RIL_NO_MEMORY: return "ril_no_memory";
            case SmsManager.RESULT_RIL_REQUEST_RATE_LIMITED: return "ril_request_rate_limited";
            case SmsManager.RESULT_RIL_INVALID_SMS_FORMAT: return "ril_invalid_sms_format";
            case SmsManager.RESULT_RIL_SYSTEM_ERR: return "ril_system_err";
            case SmsManager.RESULT_RIL_ENCODING_ERR: return "ril_encoding_err";
            case SmsManager.RESULT_RIL_INVALID_SMSC_ADDRESS: return "ril_invalid_smsc_address";
            case SmsManager.RESULT_RIL_MODEM_ERR: return "ril_modem_err";
            case SmsManager.RESULT_RIL_NETWORK_ERR: return "ril_network_err";
            case SmsManager.RESULT_RIL_INTERNAL_ERR: return "ril_internal_err";
            case SmsManager.RESULT_RIL_REQUEST_NOT_SUPPORTED: return "ril_request_not_supported";
            case SmsManager.RESULT_RIL_INVALID_MODEM_STATE: return "ril_invalid_modem_state";
            case SmsManager.RESULT_RIL_NETWORK_NOT_READY: return "ril_network_not_ready";
            case SmsManager.RESULT_RIL_OPERATION_NOT_ALLOWED: return "ril_operation_not_allowed";
            case SmsManager.RESULT_RIL_NO_RESOURCES: return "ril_no_resources";
            case SmsManager.RESULT_RIL_CANCELLED: return "ril_cancelled";
            case SmsManager.RESULT_RIL_SIM_ABSENT: return "ril_sim_absent";
            default: return "unknown";
        }
    }
    
    public static String getContactPresence(int contactPresenceId) {
        switch (contactPresenceId) {
            case ContactsContract.StatusUpdates.OFFLINE: return "offline";
            case ContactsContract.StatusUpdates.INVISIBLE: return "invisible";
            case ContactsContract.StatusUpdates.AWAY: return "away";
            case ContactsContract.StatusUpdates.IDLE: return "idle";
            case ContactsContract.StatusUpdates.DO_NOT_DISTURB: return "do_not_disturb";
            case ContactsContract.StatusUpdates.AVAILABLE: return "available";
            default: return "offline";
        }
    }
    
    public static String getContactNameStyle(int contactPresenceId) {
        switch (contactPresenceId) {
            case ContactsContract.FullNameStyle.UNDEFINED: return "unset";
            case ContactsContract.FullNameStyle.WESTERN: return "western";
            case ContactsContract.FullNameStyle.CJK: return "asian";
            case ContactsContract.FullNameStyle.CHINESE: return "chinese";
            case ContactsContract.FullNameStyle.JAPANESE: return "japanese";
            case ContactsContract.FullNameStyle.KOREAN: return "korean";
            default: return "unset";
        }
    }
    
    public static Pair<String, String> getContactDataAccount(JSONObject data) throws JSONException {
        JSONArray contactAccount = data.getJSONArray("contact_account");
        if (contactAccount.length() != 2) {
            throw new JSONException("Expected an array size of 2, got " + contactAccount.length());
        }
        return Pair.of(contactAccount.getString(0), contactAccount.getString(1));
    }

    public static String getPhoneNumberType(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Phone.TYPE_HOME: return "home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE: return "mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK: return "work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_WORK: return "fax_work";
            case ContactsContract.CommonDataKinds.Phone.TYPE_FAX_HOME: return "fax_home";
            case ContactsContract.CommonDataKinds.Phone.TYPE_PAGER: return "pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER: return "other";
            case ContactsContract.CommonDataKinds.Phone.TYPE_CALLBACK: return "callback";
            case ContactsContract.CommonDataKinds.Phone.TYPE_CAR: return "car";
            case ContactsContract.CommonDataKinds.Phone.TYPE_COMPANY_MAIN: return "company_main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_ISDN: return "isdn";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MAIN: return "main";
            case ContactsContract.CommonDataKinds.Phone.TYPE_OTHER_FAX: return "other_fax";
            case ContactsContract.CommonDataKinds.Phone.TYPE_RADIO: return "radio";
            case ContactsContract.CommonDataKinds.Phone.TYPE_TELEX: return "telex";
            case ContactsContract.CommonDataKinds.Phone.TYPE_TTY_TDD: return "tty_tdd";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_MOBILE: return "work_mobile";
            case ContactsContract.CommonDataKinds.Phone.TYPE_WORK_PAGER: return "work_pager";
            case ContactsContract.CommonDataKinds.Phone.TYPE_ASSISTANT: return "assistant";
            case ContactsContract.CommonDataKinds.Phone.TYPE_MMS: return "mms";
            default: return "other";
        }
    }

    public static String getContactEmailType(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Email.TYPE_HOME: return "home";
            case ContactsContract.CommonDataKinds.Email.TYPE_MOBILE: return "mobile";
            case ContactsContract.CommonDataKinds.Email.TYPE_WORK: return "work";
            case ContactsContract.CommonDataKinds.Email.TYPE_OTHER: return "other";
            default: return "other";
        }
    }

    public static String getContactEventType(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY: return "birthday";
            case ContactsContract.CommonDataKinds.Event.TYPE_ANNIVERSARY: return "anniversary";
            case ContactsContract.CommonDataKinds.Event.TYPE_OTHER: return "other";
            default: return "other";
        }
    }

    public static String getContactNicknameType(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.Nickname.TYPE_DEFAULT: return "default";
            case ContactsContract.CommonDataKinds.Nickname.TYPE_OTHER_NAME: return "other";
            case ContactsContract.CommonDataKinds.Nickname.TYPE_MAIDEN_NAME: return "maiden_name";
            case ContactsContract.CommonDataKinds.Nickname.TYPE_SHORT_NAME: return "short_name";
            case ContactsContract.CommonDataKinds.Nickname.TYPE_INITIALS: return "initials";
            default: return "other";
        }
    }

    public static String getContactAddressType(int type) {
        switch (type) {
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_HOME: return "home";
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_WORK: return "work";
            case ContactsContract.CommonDataKinds.StructuredPostal.TYPE_OTHER: return "other";
            default: return "other";
        }
    }

    public static String getWifiDeviceState(int wifiState) {
        switch (wifiState) {
            case WifiManager.WIFI_STATE_DISABLED: return "disabled";
            case WifiManager.WIFI_STATE_DISABLING: return "disabling";
            case WifiManager.WIFI_STATE_ENABLED: return "enabled";
            case WifiManager.WIFI_STATE_ENABLING: return "enabling";
            case WifiManager.WIFI_STATE_UNKNOWN: return "unknown";
            default: return "unknown";
        }
    }
    
    public static String getWifiConnectionStandard(int wifiStandard) {
        switch (wifiStandard) {
            case ScanResult.WIFI_STANDARD_LEGACY: return "ieee80211abg";
            case ScanResult.WIFI_STANDARD_11N: return "ieee80211n";
            case ScanResult.WIFI_STANDARD_11AC: return "ieee80211ac";
            case ScanResult.WIFI_STANDARD_11AX: return "ieee80211ax";
            case ScanResult.WIFI_STANDARD_UNKNOWN: return "unknown";
            default: return "unknown";
        }
    }
    
    public static String ipToString(int ip) {
        return "" + (ip & 0xFF) + "." + ((ip >>> 8) & 0xFF) + "." + ((ip >>> 16) & 0xFF) + "." + ((ip >>> 24) & 0xFF);
    }
}
