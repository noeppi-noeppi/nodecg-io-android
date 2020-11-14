package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import org.json.JSONException;
import org.json.JSONObject;

public interface Action {

    void apply(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException;
}
