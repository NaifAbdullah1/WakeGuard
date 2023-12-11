package com.cs407.wakeguard;

import static com.cs407.wakeguard.AlarmService.CHANNEL_ID;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class AlarmNotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Brother I Received ", Toast.LENGTH_LONG).show(); // TODO REMOVE

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        //String channelId = "alarm_service_channel";
        String channelName = "Alarm Service Channel"; // TODO Don't hardcode; put in strings.xml
        String message = intent.getStringExtra("textExtra");
        String title = intent.getStringExtra("titleExtra");
        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_HIGH);
        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        //manager.createNotificationChannel(channel);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentText(message)
                .setContentTitle(title)
                .build();
        manager.notify(0, notification); // TODO Don't hardcode the notification ID
    }
}
