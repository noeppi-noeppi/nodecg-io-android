package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.MalformedInputException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Feedback {

    public static final ScheduledExecutorService networkExecutor = new ScheduledThreadPoolExecutor(1);
    public static int nextIntentCode = 0;

    private final int port;
    private final int id;
    private boolean hasSentFeedback = false;

    public Feedback(int port, int id) {
        this.port = port;
        this.id = id;
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
        sendToNodecg(this.port, this.id, json);
    }

    public void sendFeedback(JSONObject data) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("event", false);
        json.put("data", data);
        this.sendFeedbackRaw(json);
    }

    public void sendFeedback(String msg) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put("msg", msg);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, String value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, int value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, long value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, double value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, boolean value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, JSONObject value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendFeedback(json);
    }

    public void sendFeedback(String key, JSONArray value) throws JSONException, FailureException {
        JSONObject json = new JSONObject();
        json.put(key, value);
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

    public PendingIntent getEvent(Context ctx, JSONObject data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("event", true);
        json.put("data", data);
        return PendingIntent.getBroadcast(ctx, nextIntentCode++, new Intent()
                .setAction("nodecg-io.actions.EVENT")
                .addCategory(Intent.CATEGORY_DEFAULT)
                .setClass(ctx, Events.class)
                .putExtra("evtPort", Integer.toString(this.port)) // This must be strings as android
                .putExtra("evtId", Integer.toString(this.id))     // seems to not store ints correctly.
                .putExtra("evtData", json.toString()),
                PendingIntent.FLAG_IMMUTABLE);
    }

    public PendingIntent getEvent(Context ctx, String msg) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("msg", msg);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, String value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, int value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, long value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, double value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, boolean value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, JSONObject value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public PendingIntent getEvent(Context ctx, String key, JSONArray value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        return this.getEvent(ctx, json);
    }

    public void sendError(String data) {
        if (!this.hasSentFeedback) {
            try {
                JSONObject jsonData = new JSONObject();
                jsonData.put("error_msg", data);

                JSONObject json = new JSONObject();
                json.put("success", false);
                json.put("event", false);
                json.put("data", jsonData);
                this.sendFeedbackRaw(json);
            } catch (FailureException | JSONException e) {
                Receiver.logger.warning("Failed to send error feedback: " + e.getMessage());
            }
        }
    }

    public static void sendToNodecg(int port, int id, String json) {
        networkExecutor.execute(() -> {
            try {
                URL url = new URL("http://127.0.0.1:" + port + "/");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("nodecg-io-message-id", Integer.toString(id));
                c.setDoInput(true);
                c.setDoOutput(true);
                c.connect();
                Writer writer = new OutputStreamWriter(c.getOutputStream(), StandardCharsets.UTF_8);
                writer.write(json);
                writer.write("\n");
                writer.close();
                c.getOutputStream().close();
                c.getInputStream().close();
            } catch (IOException e) {
                Receiver.logger.warning("Failed to send feedback: " + e.getMessage());
            }
        });
    }
}
