package io.github.noeppi_noeppi.nodecg_io_android;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

public class MotionSensors {

    public static void sendMotionSensorFeedback(SensorManager mgr, Feedback feedback) {
        Sensor accelerometer = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor accelerometer_u = mgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
        Sensor gravity = mgr.getDefaultSensor(Sensor.TYPE_GRAVITY);
        Sensor gyroscope = mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        Sensor gyroscope_u = mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        Sensor lin_acceleration = mgr.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        Sensor rotation = mgr.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        JSONObject json = new JSONObject();
        Set<Sensor> valuesLeft = new HashSet<>();
        AtomicBoolean failed = new AtomicBoolean(false);
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                try {
                    valuesLeft.remove(event.sensor);
                    mgr.unregisterListener(this, event.sensor);
                    if (event.sensor == accelerometer) {
                        addSensorData(json, event, Sensor.TYPE_ACCELEROMETER);
                    } else if (event.sensor == accelerometer_u) {
                        addSensorData(json, event, Sensor.TYPE_ACCELEROMETER_UNCALIBRATED);
                    } else if (event.sensor == gravity) {
                        addSensorData(json, event, Sensor.TYPE_GRAVITY);
                    } else if (event.sensor == gyroscope) {
                        addSensorData(json, event, Sensor.TYPE_GYROSCOPE);
                    } else if (event.sensor == gyroscope_u) {
                        addSensorData(json, event, Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
                    } else if (event.sensor == lin_acceleration) {
                        addSensorData(json, event, Sensor.TYPE_LINEAR_ACCELERATION);
                    } else if (event.sensor == rotation) {
                        addSensorData(json, event, Sensor.TYPE_ROTATION_VECTOR);
                    }
                    if (valuesLeft.isEmpty()) {
                        feedback.sendFeedback("motion", json);
                    }
                } catch (FailureException | JSONException e) {
                    failed.set(true);
                    feedback.sendError("Failed to gather sensor data for sensor " + event.sensor.getName() + ": " + e.getMessage());
                }
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        valuesLeft.add(accelerometer);
        valuesLeft.add(accelerometer_u);
        valuesLeft.add(gravity);
        valuesLeft.add(gyroscope);
        valuesLeft.add(gyroscope_u);
        valuesLeft.add(lin_acceleration);
        valuesLeft.add(rotation);

        mgr.registerListener(listener, accelerometer, 0);
        mgr.registerListener(listener, accelerometer_u, 0);
        mgr.registerListener(listener, gravity, 0);
        mgr.registerListener(listener, gyroscope, 0);
        mgr.registerListener(listener, gyroscope_u, 0);
        mgr.registerListener(listener, lin_acceleration, 0);
        mgr.registerListener(listener, rotation, 0);
    }

    public static void addSensorData(JSONObject json, SensorEvent event, int sensorId) throws JSONException {
        if (sensorId == Sensor.TYPE_ACCELEROMETER) {
            json.put("x", event.values[0]);
            json.put("y", event.values[1]);
            json.put("z", event.values[2]);
        } else if (sensorId == Sensor.TYPE_ACCELEROMETER_UNCALIBRATED) {
            json.put("rawX", event.values[0]);
            json.put("rawY", event.values[1]);
            json.put("rawZ", event.values[2]);
            json.put("bcX", event.values[3]);
            json.put("bcY", event.values[4]);
            json.put("bcZ", event.values[5]);
        } else if (sensorId == Sensor.TYPE_GRAVITY) {
            json.put("gravityX", event.values[0]);
            json.put("gravityY", event.values[1]);
            json.put("gravityZ", event.values[2]);
        } else if (sensorId == Sensor.TYPE_GYROSCOPE) {
            json.put("rotX", event.values[0]);
            json.put("rotY", event.values[1]);
            json.put("rotZ", event.values[2]);
        } else if (sensorId == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {
            json.put("rawRotX", event.values[0]);
            json.put("rawRotY", event.values[1]);
            json.put("rawRotZ", event.values[2]);
            json.put("driftX", event.values[3]);
            json.put("driftY", event.values[4]);
            json.put("driftZ", event.values[5]);
        } else if (sensorId == Sensor.TYPE_LINEAR_ACCELERATION) {
            json.put("ngX", event.values[0]);
            json.put("ngY", event.values[1]);
            json.put("ngZ", event.values[2]);
        } else if (sensorId == Sensor.TYPE_ROTATION_VECTOR) {
            json.put("rotVecX", event.values[0]);
            json.put("rotVecY", event.values[1]);
            json.put("rotVecZ", event.values[2]);
            if (event.values.length >= 4) {
                json.put("rotScalar", event.values[3]);
            }
        }
    }
}
