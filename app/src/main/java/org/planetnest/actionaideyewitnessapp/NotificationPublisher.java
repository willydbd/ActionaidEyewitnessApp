package org.planetnest.actionaideyewitnessapp;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * @author Banjo Mofesola Paul
 *         Chief Developer, Planet NEST
 *         mofesolapaul@planetnest.org
 *         12/12/2017 07:51
 */

public class NotificationPublisher extends BroadcastReceiver {
    public static String NOTIFICATION_ID = "aan-evtcapture-app-notif";
    public static String NOTIFICATION = "notification";

    public void onReceive(Context context, Intent intent) {
Log.e("MOFE", "Notif received");
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Notification notification = intent.getParcelableExtra(NOTIFICATION);
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        notificationManager.notify(id, notification);

    }
}
