package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.*;
import java.util.function.Consumer;

public class Subscription {
    
    private static final Map<UUID, Subscription> activeSubscriptions = new HashMap<>();
    private static boolean foregroundServiceRunning = false;
    
    public final UUID id = UUID.randomUUID();
    private final int portOfSubscriber;
    private final Feedback events;
    private final List<Consumer<Context>> cancellationHandler;
    private boolean cancelled = false;
    
    private Subscription(Feedback events) {
        this.events = events;
        this.portOfSubscriber = events.getPort();
        this.cancellationHandler = new ArrayList<>();
    }
    
    public void addCancellationHandler(Consumer<Context> handler) {
        this.cancellationHandler.add(handler);
    }

    public void sendEvent(JSONObject data) throws JSONException, FailureException {
        if (!this.cancelled) {
            this.events.sendEvent(data);
        }
    }

    public void sendEvent(String msg) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(msg);
        }
    }

    public void sendEvent(String key, String value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, int value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, long value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, double value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, boolean value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, JSONObject value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }

    public void sendEvent(String key, JSONArray value) throws JSONException {
        if (!this.cancelled) {
            this.events.sendEvent(key, value);
        }
    }
    
    public static Subscription create(Context ctx, Feedback feedback) throws JSONException, FailureException {
        Subscription subscription = new Subscription(feedback);
        activeSubscriptions.put(subscription.id, subscription);
        feedback.sendFeedback("subscription_id", subscription.id.toString());
        startForegroundService(ctx);
        return subscription;
    }
    
    public static void cancel(Context ctx, Subscription subscription) {
        if (!subscription.cancelled) {
            subscription.cancelled = true;
            subscription.cancellationHandler.forEach(handler -> handler.accept(ctx));
            activeSubscriptions.remove(subscription.id);
            if (activeSubscriptions.isEmpty()) {
                stopForegroundService(ctx);
            }
            Receiver.logger.info("Cancelled subscription: " + subscription.id);
        }
    }
    
    public static void cancel(Context ctx, UUID subscription) {
        if (activeSubscriptions.containsKey(subscription)) {
            cancel(ctx, Objects.requireNonNull(activeSubscriptions.get(subscription)));
        }
    }
    
    public static void cancelAll(Context ctx, int port) {
        Iterator<Subscription> itr = activeSubscriptions.values().iterator();
        while (itr.hasNext()) {
            Subscription subscription = itr.next();
            if (!subscription.cancelled && subscription.portOfSubscriber == port) {
                subscription.cancelled = true;
                subscription.cancellationHandler.forEach(handler -> handler.accept(ctx));
                itr.remove();
            }
        }
        if (activeSubscriptions.isEmpty()) {
            stopForegroundService(ctx);
        }
        Receiver.logger.info("Cancelled all subscriptions of subscriber at port " + port);
    }

    private static void startForegroundService(Context ctx) {
        if (!foregroundServiceRunning) {
            foregroundServiceRunning = true;
            Intent serviceIntent = new Intent().setAction("nodecg-io.actions.START_FG")
                    .addCategory("android.intent.category.DEFAULT")
                    .setClass(ctx, SubscriptionForegroundService.class);
            
            ctx.startForegroundService(serviceIntent);
        }
    }
    
    private static void stopForegroundService(Context ctx) {
        if (foregroundServiceRunning) {
            foregroundServiceRunning = false;
            Intent serviceIntent = new Intent().setAction("nodecg-io.actions.START_FG")
                    .addCategory("android.intent.category.DEFAULT")
                    .setClass(ctx, SubscriptionForegroundService.class);

            ctx.stopService(serviceIntent);
        }
    }
}
