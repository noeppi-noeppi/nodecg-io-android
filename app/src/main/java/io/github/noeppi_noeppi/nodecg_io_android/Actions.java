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
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.util.Base64;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.*;
import io.github.noeppi_noeppi.nodecg_io_android.contentresolver.data.*;
import org.apache.commons.lang3.tuple.Pair;
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
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, time, distance, listener);
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
    
    public static void magneticField(Context ctx, JSONObject data, Feedback feedback) throws FailureException {
        SensorManager mgr = ctx.getSystemService(SensorManager.class);
        Sensor sensor = mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Feedback delayed = Feedback.delay(feedback);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mgr.unregisterListener(this);
                try {
                    JSONObject json = new JSONObject();
                    json.put("x", event.values[0]);
                    json.put("y", event.values[1]);
                    json.put("z", event.values[2]);
                    delayed.sendFeedback("magnetic_field", json);
                } catch (JSONException | FailureException e) {
                    delayed.sendError("Failed: " + e.getMessage());
                    Receiver.logger.warning("Failed to send magnetic field values: " + e.getMessage());
                }
                Receiver.logger.info(Arrays.toString(event.values));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mgr.registerListener(listener, sensor, 0);
    }
    
    public static void ambientLight(Context ctx, JSONObject data, Feedback feedback) throws FailureException {
        SensorManager mgr = ctx.getSystemService(SensorManager.class);
        Sensor sensor = mgr.getDefaultSensor(Sensor.TYPE_LIGHT);
        Feedback delayed = Feedback.delay(feedback);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                mgr.unregisterListener(this);
                try {
                    delayed.sendFeedback("light", event.values[0]);
                } catch (JSONException | FailureException e) {
                    delayed.sendError("Failed: " + e.getMessage());
                    Receiver.logger.warning("Failed to send light value: " + e.getMessage());
                }
                Receiver.logger.info(Arrays.toString(event.values));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };
        mgr.registerListener(listener, sensor, 0);
    }
    
    public static void getTelephonies(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Set<Integer> ids = Helper.getTelephonyIds(ctx);
        JSONArray array = new JSONArray();
        for (int subId : ids) {
            array.put(subId);
        }
        feedback.sendFeedback("telephonies", array);
    }
    
    public static void getTelephonyProperties(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        SubscriptionInfo subInfo = Helper.getTelephony(ctx, data);
        TelephonyManager mgr = Helper.getTelephonyManager(ctx, subInfo);
        JSONObject json = new JSONObject();
        
        int simSlotIndex = subInfo.getSimSlotIndex();
        if (simSlotIndex != SubscriptionManager.INVALID_SIM_SLOT_INDEX) {
            json.put("simSlot", simSlotIndex);
        }
        
        json.put("name", subInfo.getDisplayName().toString());
        
        String mcc = subInfo.getMccString();
        if (mcc != null && !mcc.isEmpty()) {
            json.put("countryCode", mcc);
        }
        
        String mnc = subInfo.getMncString();
        if (mnc != null && !mnc.isEmpty()) {
            json.put("networkCode", mnc);
        }
        
        String iso = subInfo.getCountryIso();
        if (iso != null && !iso.isEmpty()) {
            json.put("countryISO", iso);
        }
        
        json.put("embedded", subInfo.isEmbedded());
        
        String number = mgr.getLine1Number();
        if (number != null && !number.isEmpty()) {
            json.put("number", number);
        }
        
        String manufacturerCode = mgr.getManufacturerCode();
        if (manufacturerCode != null && !manufacturerCode.isEmpty()) {
            json.put("manufacturerCode", manufacturerCode);
        }
        
        feedback.sendFeedback("properties", json);
    }

    public static void getTelephonyForMessage(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.PHONE);
        long telephonyId = data.getLong("telephony_id");
        feedback.sendFeedback("available", telephonyId >= 0 && Helper.getTelephonyIds(ctx).contains((int) telephonyId));
    }
    
    public static void getSMS(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.READ_SMS);
        ContentType<Sms> type = Helper.getSmsType(data);
        AppliedFilter<?> filter = Helper.getSMSFilter(ctx, data);
        ContentProvider<Sms> provider = new ContentProvider<>(ctx, type);
        ResultSet<Sms> result = provider.query(filter);
        List<Sms> smsList = result.getDataList();
        JSONArray array = new JSONArray();
        for (Sms sms : smsList) {
            array.put(sms.toJSON());
        }
        feedback.sendFeedback("sms", array);
    }

    public static void getMMS(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.READ_SMS);
        ContentType<Mms> type = Helper.getMmsType(data);
        AppliedFilter<?> filter = Helper.getSMSFilter(ctx, data);
        ContentProvider<Mms> provider = new ContentProvider<>(ctx, type);
        ResultSet<Mms> result = provider.query(filter);
        List<Mms> mmsList = result.getDataList();
        JSONArray array = new JSONArray();
        for (Mms mms : mmsList) {
            array.put(mms.toJSON());
        }
        feedback.sendFeedback("sms", array);
    }
    
    public static void getThreadForMessage(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.READ_SMS);
        long threadId = data.getLong("thread_id");
        MessageThread thread = threadId < 0 ? null : new ContentProvider<>(ctx, ContentType.MESSAGE_THREAD).query(ContentFilter.BY_ID, threadId).head();
        if (thread == null) {
            feedback.sendFeedback("available", false);
        } else {
            JSONObject json = new JSONObject();
            json.put("available", true);
            json.put("thread", thread.toJSON());
            feedback.sendFeedback(json);
        }
    }
    
    public static void getSmsRecipient(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.READ_SMS);
        long senderId = data.getLong("sender_id");
        Recipient recipient = new ContentProvider<>(ctx, ContentType.RECIPIENT).query(ContentFilter.BY_ID, senderId).head();
        if (recipient == null) {
            feedback.sendFeedback("available", false);
        } else {
            JSONObject json = new JSONObject();
            json.put("available", true);
            json.put("recipient", recipient.toJSON());
            feedback.sendFeedback(json);
        }
    }
    
    public static void getThreadRecipients(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.READ_SMS);
        long threadId = data.getLong("id");
        MessageThread thread = new ContentProvider<>(ctx, ContentType.MESSAGE_THREAD).query(ContentFilter.BY_ID, threadId).head();
        if (thread == null) {
            throw new FailureException("Unknown thread: " + threadId);
        }
        if (thread.recipients == null) {
            throw new FailureException("Could not query thread recipients for thread: " + thread._id + " (" + threadId + ")");
        }
        Set<Long> recipientIds = Arrays.stream(thread.recipients.split(" ")).map(String::trim).map(Long::parseLong).collect(Collectors.toSet());
        List<Recipient> recipients = new ContentProvider<>(ctx, ContentType.RECIPIENT).query(ContentFilter.BY_IDS, recipientIds).getDataList();
        JSONArray array = new JSONArray();
        for (Recipient recipient : recipients) {
            array.put(recipient.toJSON());
        }
        feedback.sendFeedback("recipients", array);
    }
    
    public static void sendSMS(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.SEND_SMS, Permission.PHONE);
        String address = data.getString("address");
        String text = data.getString("text");
        if (!text.trim().isEmpty()) {
            SmsManager mgr = Helper.getSmsManager(ctx, Helper.getTelephony(ctx, data));
            ArrayList<String> parts = mgr.divideMessage(text);
            PendingIntent sentIntent = feedback.getEvent(ctx, EventGenerator.SMS_SENT);
            JSONObject deliverJson = new JSONObject();
            deliverJson.put("type", "delivered");
            PendingIntent deliverIntent = feedback.getEvent(ctx, deliverJson);
            if (parts.size() == 1) {
                mgr.sendTextMessage(address, null, text, sentIntent, deliverIntent);
            } else {
                ArrayList<PendingIntent> sentList = new ArrayList<>();
                ArrayList<PendingIntent> deliverList = new ArrayList<>();
                for (int i = 0; i < parts.size(); i++) {
                    sentList.add(sentIntent);
                    deliverList.add(deliverIntent);
                }
                mgr.sendMultipartTextMessage(address, null, parts, sentList, deliverList);
            }
        }
    }

    public static void getAllContacts(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.CONTACTS);
        List<Contact> contacts = new ContentProvider<>(ctx, ContentType.CONTACT).query().getDataList();
        JSONArray array = new JSONArray();
        for (Contact contact : contacts) {
            array.put(contact.toJSON());
        }
        feedback.sendFeedback("contact_ids", array);
    }
    
    public static void contactStatus(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.CONTACTS);
        long contactId = data.getLong("id");
        ContactStatus status = new ContentProvider<>(ctx, ContentType.CONTACT_STATUS).query(ContentFilter.BY_ID, contactId).head();
        if (status == null) {
            throw new FailureException("No contact found with id " + contactId);
        }
        feedback.sendFeedback("status", status.toJSON());
    }

    public static void contactName(Context ctx, JSONObject data, Feedback feedback) throws FailureException, JSONException {
        Permissions.ensure(ctx, Permission.CONTACTS);
        long contactId = data.getLong("id");
        Contact contact = new ContentProvider<>(ctx, ContentType.CONTACT).query(ContentFilter.BY_ID, contactId).head();
        if (contact == null) {
            throw new FailureException("No contact found with id " + contactId);
        }
        long nameId = contact.nameInfoId;
        ContactName name = new ContentProvider<>(ctx, ContentType.CONTACT_NAME).query(ContentFilter.and(ContentFilter.BY_CONTACTS_RAW_ID, nameId, ContentFilter.CONTACTS_MIME, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)).head();
        if (name == null) {
            throw new FailureException("No contact name found with id " + nameId + " (" + contactId + ")");
        }
        feedback.sendFeedback("name", name.toJSON());
    }
}
