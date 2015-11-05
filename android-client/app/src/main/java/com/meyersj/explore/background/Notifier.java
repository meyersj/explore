package com.meyersj.explore.background;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;

import com.meyersj.explore.R;
import com.meyersj.explore.activity.MainActivity;
import com.meyersj.explore.utilities.Cons;


public class Notifier {

    private Service service;
    private Context context;

    public Notifier(Service service, Context context) {
        this.service = service;
        this.context = context;
    }

    public void scanningNotification() {
        int icon = R.drawable.ic_track_changes_white_24dp;
        String title = "Explore";
        String content = "Scanning for locations";
        NotificationCompat.Builder mBuilder = getBuilder(icon, title, content);
        PendingIntent resultPendingIntent = getIntent(new Intent(service, MainActivity.class));
        mBuilder.setContentIntent(resultPendingIntent);
        sendNotification(0, mBuilder);
    }

    private void sendNotification(int id, NotificationCompat.Builder builder) {
        NotificationManager mNotifyMgr = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotifyMgr.notify(id, builder.build());
    }

    private NotificationCompat.Builder getBuilder(int icon, String title, String content) {
        return new NotificationCompat.Builder(context)
                .setSmallIcon(icon)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);
    }

    private PendingIntent getIntent(Intent intent) {
        return PendingIntent.getActivity(service, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public void beaconNotification(int id, String beaconName, Bundle extras) {
        int icon = R.drawable.ic_location_city_white_24dp;
        String title = "New Location";
        String content = "Touch to register it.";
        if (extras.getBoolean(Cons.REGISTERED)) {
            icon = R.drawable.ic_chat_white_24dp;
            title = beaconName;
            content = "Touch to add a message.";
        }
        NotificationCompat.Builder mBuilder = getBuilder(icon, title, content);
        Intent resultIntent = new Intent(service, MainActivity.class);
        extras.putBoolean(Cons.NOTIFICATION, true);
        resultIntent.putExtras(extras);
        PendingIntent resultPendingIntent = getIntent(resultIntent);
        mBuilder.setContentIntent(resultPendingIntent);
        vibrate(mBuilder);
        sendNotification(id, mBuilder);
    }

    private void vibrate(NotificationCompat.Builder builder) {
        Vibrator v = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        v.vibrate(2500);
        builder.setVibrate(new long[] { 0, 1000, 500, 1000});
        builder.setLights(Color.YELLOW, 3000, 3000);
    }
}
