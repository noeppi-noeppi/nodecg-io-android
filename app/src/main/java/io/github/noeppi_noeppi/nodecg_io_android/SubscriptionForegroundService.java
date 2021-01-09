package io.github.noeppi_noeppi.nodecg_io_android;

import android.app.*;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import androidx.annotation.Nullable;

public class SubscriptionForegroundService extends Service {

    public static final int ONGOING_NOTIFICATION_ID = 18921;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int code = super.onStartCommand(intent, flags, startId);

        NotificationChannel channel = new NotificationChannel("nodecg-io-foreground-service", "nodecg-io", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLightColor(0xff7a04);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager mgr = this.getSystemService(NotificationManager.class);
        mgr.createNotificationChannel(channel);
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        Notification notification = new Notification.Builder(this, "nodecg-io-foreground-service")
                .setContentTitle(this.getText(R.string.foreground_service_title))
                .setContentText(this.getText(R.string.foreground_service_text))
                .setSmallIcon(R.drawable.logo_round)
                .setContentIntent(pendingIntent)
                .build();

        this.startForeground(ONGOING_NOTIFICATION_ID, notification);
        return code;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
