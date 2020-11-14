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
            logger.warning("Received invalid Intent. Malformed Json: " + e.getMessage());
            return;
        }

        int port;
        int id;
        try {
            try {
                port = intent.getIntExtra("port", Integer.MIN_VALUE);
            } catch (ClassCastException e) {
                port = Integer.MIN_VALUE;
            }
            if (port == Integer.MIN_VALUE) {
                port = Integer.parseInt(intent.getStringExtra("port"));
            }

            try {
                id = intent.getIntExtra("id", Integer.MIN_VALUE);
            } catch (ClassCastException e) {
                id = Integer.MIN_VALUE;
            }
            if (id == Integer.MIN_VALUE) {
                id = Integer.parseInt(intent.getStringExtra("id"));
            }
        } catch (NumberFormatException e) {
            logger.warning("Received invalid Intent. Invalid Integer: " + e.getMessage());
            return;
        }
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
                }
            }
        } else {
            logger.warning("Received invalid Intent. Invalid Action: " + actionId);
        }
    }
}
