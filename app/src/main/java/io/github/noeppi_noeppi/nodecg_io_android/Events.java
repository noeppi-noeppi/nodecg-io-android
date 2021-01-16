package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import java.util.Arrays;

import static io.github.noeppi_noeppi.nodecg_io_android.Receiver.logger;

// Internally used to listen to android events and send them back to nodecg
public class Events extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            logger.info("Received Event Intent: " + intent);
            logger.info("Extras: " + Arrays.toString(intent.getExtras().keySet().toArray()));
            int port;
            int id;
            try {
                port = Integer.parseInt(intent.getStringExtra("evtPort"));
                id = Integer.parseInt(intent.getStringExtra("evtId"));
            } catch (NumberFormatException e) {
                logger.warning("Internal Error: Received invalid Event Intent. Invalid Integer: " + e.getMessage());
                return;
            }
            System.out.println("EVTPORT: " + port);
            EventGenerator gen = EventGenerator.valueOf(intent.getStringExtra("evtGenerator"));
            Feedback.sendToNodecg(port, id, gen.get(this.getResultCode(), intent));
        } catch (Exception e) {
            logger.warning("Internal Error: Received invalid Event Intent: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}
