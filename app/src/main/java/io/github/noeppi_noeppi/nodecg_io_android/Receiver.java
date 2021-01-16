package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Arrays;
import java.util.Map;
import java.util.logging.Logger;

public class Receiver extends BroadcastReceiver {

    public static final Logger logger = Logger.getLogger("nodecg-io");

    // Call it
    // am broadcast -a nodecg-io.actions.ACT -c android.intent.category.DEFAULT -n "io.github.noeppi_noeppi.nodecg_io_android/io.github.noeppi_noeppi.nodecg_io_android.Receiver" -e param value

    private final Map<String, Action> actions = ImmutableMap.<String, Action>builder()
            .put("ping", Actions::ping)
            .put("request_permissions", Actions::requestPermissions)
            .put("check_availability", Actions::checkAvailability)
            .put("show_toast", Actions::showToast)
            .put("cancel_subscription", Actions::cancelSubscription)
            .put("cancel_all_subscriptions", Actions::cancelAllSubscriptions)
            .put("get_volume", Actions::getVolume)
            .put("get_max_volume", Actions::getMaxVolume)
            .put("set_volume", Actions::setVolume)
            .put("adjust_volume", Actions::adjustVolume)
            .put("wake_up", Actions::wakeUp)
            .put("get_packages", Actions::getPackages)
            .put("get_package", Actions::getPackage)
            .put("get_activities", Actions::getActivities)
            .put("get_activity", Actions::getActivity)
            .put("start_activity", Actions::startActivity)
            .put("get_package_version", Actions::getPackageVersion)
            .put("notify", Actions::notify)
            .put("gps_active", Actions::gpsActive)
            .put("gps_last_known_location", Actions::gpsLastKnownLocation)
            .put("gps_subscribe", Actions::gpsSubscribe)
            .put("motion_current", Actions::motionCurrent)
            .put("motion_subscribe", Actions::motionSubscribe)
            .put("magnetic_field", Actions::magneticField)
            .put("ambient_light", Actions::ambientLight)
            .put("get_telephonies", Actions::getTelephonies)
            .put("get_telephony_properties", Actions::getTelephonyProperties)
            .put("get_telephony_for_message", Actions::getTelephonyForMessage)
            .put("get_sms", Actions::getSMS)
            .put("get_mms", Actions::getMMS)
            .put("get_thread_for_message", Actions::getThreadForMessage)
            .put("get_sms_recipient", Actions::getSmsRecipient)
            .put("get_thread_recipients", Actions::getThreadRecipients)
            .put("send_sms", Actions::sendSMS)
            .put("get_all_contacts", Actions::getAllContacts)
            .put("contact_status", Actions::contactStatus)
            .put("contact_name", Actions::contactName)
            .build();

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.info("Received Intent: " + intent);
        logger.info("Extras: " + Arrays.toString(intent.getExtras().keySet().toArray()));
        if (!intent.hasExtra("action") || !intent.hasExtra("data") || !intent.hasExtra("port") || !intent.hasExtra("id")) {
            logger.warning("Received invalid Intent. A mandatory attribute is missing.");
            return;
        }
        String actionId = intent.getStringExtra("action").toLowerCase();
        JSONObject data = null;
        try {
            String dataRaw = intent.getStringExtra("data");
            Object dataElem = new JSONTokener(dataRaw).nextValue();
            if (dataElem instanceof JSONObject) {
                data = (JSONObject) dataElem;
            } else {
                logger.warning("Received invalid Intent. Json is not an object.");
            }
        } catch (JSONException e) {
            logger.warning("Received invalid Intent. Invalid Json: " + e.getMessage());
            return;
        }

        int port;
        int id;
        try {
            port = Integer.parseInt(intent.getStringExtra("port"));
            id = Integer.parseInt(intent.getStringExtra("id"));
        } catch (NumberFormatException e) {
            logger.warning("Received invalid Intent. Invalid Integer: " + e.getMessage());
            return;
        }

        System.out.println("PORT: " + port);
        
        Feedback feedback = new Feedback(port, id);

        if (this.actions.containsKey(actionId)) {
            Action action = this.actions.get(actionId);
            if (action != null) {
                try {
                    action.apply(context, data, feedback);
                    feedback.sendOptionalFeedback(new JSONObject());
                } catch (JSONException e) {
                    logger.warning("Received invalid Intent. Invalid Json while processing action " + actionId + ":  " + e.getMessage());
                    feedback.sendError("Received invalid Intent. Invalid Json while processing action " + actionId + ":  " + e.getMessage());
                } catch (FailureException e) {
                    logger.warning("Failed to process intent of type " + actionId + ": " + e.getMessage());
                    feedback.sendError("Failed to process intent of type " + actionId + ": " + e.getMessage());
                } catch (RuntimeException e) {
                    logger.warning("Intent of type " + actionId + " threw an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                    feedback.sendError("Intent of type " + actionId + " threw an exception: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                } catch (Throwable t) {
                    feedback.sendError("Internal error");
                    throw t;
                }
            } else {
                feedback.sendError("Action not found.");
            }
        } else {
            logger.warning("Received invalid Intent. Invalid Action: " + actionId);
        }
    }
}
