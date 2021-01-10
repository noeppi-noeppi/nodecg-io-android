package io.github.noeppi_noeppi.nodecg_io_android;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.annotation.Nullable;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;

public class Feedback {

    public static final ScheduledExecutorService networkExecutor = new ScheduledThreadPoolExecutor(1);
    public static int nextIntentCode = 0;

    // Create a trust manager that does not validate certificate chains
    private static final TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {

                }

                @SuppressLint("TrustAllX509TrustManager")
                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {

                }
            }
    };

    private static final SSLContext ssl;
    static {
        try {
            ssl = SSLContext.getInstance("SSL");
            ssl.init(null, trustAllCerts, new java.security.SecureRandom()); 
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new RuntimeException("Could not create SSLContext", e);
        }
    }

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

    public void sendEvent(JSONObject data) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("success", true);
        json.put("event", true);
        json.put("data", data);
        sendToNodecg(this.port, this.id, json.toString());
    }

    public void sendEvent(String msg) throws JSONException {
        JSONObject json = new JSONObject();
        json.put("msg", msg);
        this.sendEvent(json);
    }

    public void sendEvent(String key, String value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, int value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, long value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, double value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, boolean value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, JSONObject value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
    }

    public void sendEvent(String key, JSONArray value) throws JSONException {
        JSONObject json = new JSONObject();
        json.put(key, value);
        this.sendEvent(json);
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

    public int getPort() {
        return this.port;
    }

    public static void sendToNodecg(int port, int id, String json) {
        networkExecutor.execute(() -> {
            try {
                URL url = new URL("http://127.0.0.1:" + port + "/");
                HttpURLConnection c = (HttpURLConnection) url.openConnection();
                //c.setSSLSocketFactory(ssl.getSocketFactory());
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
    
    // Attaches a feedback to an intent. This will also mark the feedback as invalid so no default
    // feedback is sent. Make sure you send a feedback in any case.
    public static Intent attach(Intent intent, Feedback feedback) throws FailureException {
        if (feedback.hasSentFeedback) {
            throw new FailureException("Tried to add an invalid feedback to an intent.");
        }
        intent.putExtra("io.github.noeppi_noeppi.nodecg_io_android.FEEDBACK", new int[]{ feedback.port, feedback.id });
        feedback.hasSentFeedback = true;
        return intent;
    }
    
    // Gets a feedback from an intent. Can only be used once.
    @Nullable
    public static Feedback get(Intent intent) {
        int[] data = intent.getIntArrayExtra("io.github.noeppi_noeppi.nodecg_io_android.FEEDBACK");
        if (data == null || data.length != 2) {
            return null;
        }
        intent.removeExtra("io.github.noeppi_noeppi.nodecg_io_android.FEEDBACK");
        return new Feedback(data[0], data[1]);
    }
    
    // Gets a new feedback and invalidates the old so you can send it later.
    public static Feedback delay(Feedback feedback) throws FailureException {
        if (feedback.hasSentFeedback) {
            throw new FailureException("Tried delay an invalid feedback.");
        }
        feedback.hasSentFeedback = true;
        return new Feedback(feedback.port, feedback.id);
    }
}
