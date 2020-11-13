package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import com.google.common.collect.ImmutableMap;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.logging.Logger;

public class Receiver extends BroadcastReceiver {

    public static final Logger logger = Logger.getLogger("nodecg-io");

    // Call it
    // am broadcast -a nodecg-io.actions.ACT -c android.intent.category.DEFAULT -n "io.github.noeppi_noeppi.nodecg_io_android/io.github.noeppi_noeppi.nodecg_io_android.Receiver" -e param value

    private final Map<String, Action> actions = ImmutableMap.<String, Action>builder()
            .put("ping", Actions::ping)
            .build();

    @Override
    public void onReceive(Context context, Intent intent) {
        logger.fine("Received Intent: " + intent);
        if (!intent.hasExtra("action") || !intent.hasExtra("data") || !intent.hasExtra("url")) {
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
        URL url = null;
        try {
            url = new URL(intent.getStringExtra("url"));
        } catch (MalformedURLException e) {
            logger.warning("Received invalid Intent. Invalid feedback URL.");
        }
        Feedback feedback = new Feedback(url);

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
            feedback.sendError("Received invalid Intent. Invalid Action: " + actionId);
        }
    }
}
