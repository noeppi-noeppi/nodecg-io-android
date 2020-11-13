package io.github.noeppi_noeppi.nodecg_io_android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class Receiver extends BroadcastReceiver {

    // Call it
    // am broadcast -a nodecg-io.actions.ACT -c android.intent.category.DEFAULT -n "io.github.noeppi_noeppi.nodecg_io_android/io.github.noeppi_noeppi.nodecg_io_android.Receiver" -e param value

    public Receiver() {
        super();
        System.out.println("RECEIVER CREATED WHOO!!!");
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        System.out.println("INTENT CALLED: " + intent);
        if (intent.hasExtra("action")) {
            System.out.println("INTENT ACTION: " + intent.getStringExtra("action"));
        }
    }
}
