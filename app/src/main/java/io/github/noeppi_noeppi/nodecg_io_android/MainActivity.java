package io.github.noeppi_noeppi.nodecg_io_android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity { 
    
    public static final int ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE = 2323;

    public MainActivity() {
        super();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.requestAlertPermission();
        this.requestIntentPermissions();

        SensorManager mgr = this.getSystemService(SensorManager.class);
        Receiver.logger.info("LIGHT: " + mgr.getDefaultSensor(Sensor.TYPE_LIGHT));
        Receiver.logger.info("MAGNETIC_FIELD: " + mgr.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD));
        Receiver.logger.info("PROXIMITY: " + mgr.getDefaultSensor(Sensor.TYPE_PROXIMITY));
        Receiver.logger.info("STEP_COUNTER: " + mgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER));
    }
    
    private void requestAlertPermission() {
        if (!Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            //noinspection deprecation
            this.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE);
        }
    }
    
    private void requestIntentPermissions() {
        Intent intent = this.getIntent();
        String[] permissions = intent.getStringArrayExtra("io.github.noeppi_noeppi.nodecg_io_android.REQUEST_PERMISSIONS");
        Feedback feedback = Feedback.get(intent);
        if (permissions != null && permissions.length > 0 && feedback != null) {
            ActivityResultLauncher<String[]> requestPermissionLauncher = this.registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), map -> {
                Set<String> left = new HashSet<>(Arrays.asList(permissions));
                map.entrySet().stream().filter(Map.Entry::getValue).map(Map.Entry::getKey).forEach(left::remove);
                try {
                    if (left.isEmpty()) {
                        feedback.sendFeedback("success", true);
                        this.finish();
                    } else {
                        JSONObject json = new JSONObject();
                        json.put("success", false);
                        json.put("errmsg", "Missing permissions: " + String.join(", ", left));
                        feedback.sendFeedback(json);
                    }
                } catch (FailureException | JSONException e) {
                    feedback.sendOptionalFeedback(new JSONObject());
                }
            });
            requestPermissionLauncher.launch(permissions);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ACTION_MANAGE_OVERLAY_PERMISSION_REQUEST_CODE) {
            if (!Settings.canDrawOverlays(this)) {
                Toast.makeText(this, R.string.alert_permission_required, Toast.LENGTH_LONG).show();
                this.finish();
            }
        }
    }
}