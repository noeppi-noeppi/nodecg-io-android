package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.Intent;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;

// Special values to generate the event object
public enum EventGenerator {

    DEFAULT((r, i) -> i.getStringExtra("evtData"), false),
    SMS_SENT((r, i) -> {
        JSONObject data = new JSONObject();
        data.put("type", "sent");
        data.put("code", Helper.getSmsResult(r));
        return data;
    });

    private final BiFunction<Integer, Intent, String> resultFunc;

    EventGenerator(BiFunction<Integer, Intent, String> resultFunc, boolean unused) {
        this.resultFunc = resultFunc;
    }

    EventGenerator(BiFunctionJSON<Integer, Intent, JSONObject> resultFunc) {
        this.resultFunc = resultFunc.andThen(data -> {
            try {
                JSONObject json = new JSONObject();
                json.put("success", true);
                json.put("event", true);
                json.put("data", data);
                return json.toString();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public String get(int resultCode, Intent receivedIntent) {
        return this.resultFunc.apply(resultCode, receivedIntent);
    }

    private interface BiFunctionJSON<T, U, R> {

        R apply(T t, U u) throws JSONException;

        default <V> BiFunction<T, U, V> andThen(Function<? super R, ? extends V> after) {
            Objects.requireNonNull(after);
            return (T t, U u) -> {
                try {
                    return after.apply(this.apply(t, u));
                } catch (JSONException e) {
                    throw new RuntimeException(e);
                }
            };
        }
    }
}
