package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

import static io.github.noeppi_noeppi.nodecg_io_android.Receiver.logger;

public class Events extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            logger.info("Received Event Intent: " + intent);
            logger.info("Extras: " + Arrays.toString(intent.getExtras().keySet().toArray()));
            if (intent.hasExtra("evtPort") && intent.hasExtra("evtId") && intent.hasExtra("evtData")) {
                int port;
                int id;
                try {
                    port = intent.getIntExtra("evtPort", Integer.MIN_VALUE);
                } catch (ClassCastException e) {
                    port = Integer.MIN_VALUE;
                }
                if (port == Integer.MIN_VALUE) {
                    port = Integer.parseInt(intent.getStringExtra("evtPort"));
                }

                try {
                    id = intent.getIntExtra("evtId", Integer.MIN_VALUE);
                } catch (ClassCastException e) {
                    id = Integer.MIN_VALUE;
                }
                if (id == Integer.MIN_VALUE) {
                    id = Integer.parseInt(intent.getStringExtra("evtId"));
                }
                Feedback.sendToNodecg(port, id, intent.getStringExtra("evtData"));
            }
        } catch (Exception e) {
            //
        }
    }
}
