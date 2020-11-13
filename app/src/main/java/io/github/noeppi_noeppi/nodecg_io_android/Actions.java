package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public class Actions {

    public static void ping(Context ctx, JSONObject data, Feedback resp) throws JSONException, FailureException {
        resp.sendFeedback("pong");
    }
}
