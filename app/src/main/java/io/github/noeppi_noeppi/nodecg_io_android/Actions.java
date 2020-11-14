package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Context;
import android.media.AudioManager;
import android.os.PowerManager;
import org.json.JSONException;
import org.json.JSONObject;

public class Actions {

    private static PowerManager.WakeLock wakeLock = null;

    public static void ping(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        Receiver.logger.info("Ping received. nodecg-io-android is right here!");
        feedback.sendFeedback("pong");
    }

    public static void setVolume(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        String channel = FailureException.nn(data.getString("channel").toLowerCase());
        int stream = Helper.getAudioStream(channel);

        AudioManager audio = ctx.getSystemService(AudioManager.class);

        int max = audio.getStreamMaxVolume(stream);
        int vol = (int) Math.round(data.getDouble("volume") * max);

        int flags = 0;
        if (data.getBoolean("play_sound")) {
            flags |= AudioManager.FLAG_PLAY_SOUND;
        }
        if (data.getBoolean("show_ui")) {
            flags |= AudioManager.FLAG_SHOW_UI;
        }

        audio.setStreamVolume(stream, vol, flags);
    }

    public static void wakeUp(Context ctx, JSONObject data, Feedback feedback) throws JSONException, FailureException {
        PowerManager power = ctx.getSystemService(PowerManager.class);
        if (wakeLock == null) {
            wakeLock = power.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "nodecg-io:wakelock");
        }
        wakeLock.acquire(1000);
        wakeLock.release();
    }
}
