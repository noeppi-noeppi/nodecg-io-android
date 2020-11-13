package io.github.noeppi_noeppi.nodecg_io_android;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

public class Feedback {

    private final URL url;
    private boolean hasSentFeedback = false;

    public Feedback(URL url) {
        this.url = url;
    }

    public boolean hasSentFeedback() {
        return this.hasSentFeedback;
    }

    private void sendFeedbackRaw(JSONObject data) throws FailureException {
        if (this.hasSentFeedback) {
            throw new FailureException("Already sent feedback.");
        }
        this.hasSentFeedback = true;
        String json = data.toString();
        try {
            URLConnection c = this.url.openConnection();
            c.setDoInput(true);
            c.setDoOutput(true);
            c.connect();
            c.getOutputStream().write(StandardCharsets.UTF_8.encode(json + "\n").array());
            c.getOutputStream().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void sendFeedback(JSONObject data) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("data", data);
        this.sendFeedbackRaw(json);
    }

    public void sendFeedback(String msg) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put("msg", msg);
        this.sendFeedback(json);
    }

    public void sendOptionalFeedback(JSONObject data) {
        if (!this.hasSentFeedback) {
            try {
                this.sendFeedback(data);
            } catch (FailureException e) {
                throw new Error(); // Should never happen
            } catch (JSONException e) {
                Receiver.logger.warning("Failed to send optional feedback: JSONException: " + e.getMessage());
            }
        }
    }

    public void sendError(String data) {
        if (!this.hasSentFeedback) {
            try {
                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("error", data);
                this.sendFeedbackRaw(json);
            } catch (FailureException | JSONException e) {
                Receiver.logger.warning("Failed to send error feedback: " + e.getMessage());
            }
        }
    }
}
