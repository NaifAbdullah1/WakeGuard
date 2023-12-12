package com.cs407.wakeguard;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.app.RemoteInput;

import java.util.ArrayList;

public class NotificationHelper {
    private static final NotificationHelper INSTANCE = new NotificationHelper();

    private NotificationHelper() {}

    public static NotificationHelper getInstance() {
        return INSTANCE;
    }

    public static final String CHANNEL_ID = "channel_chat";
    public static final String TEXT_REPLY = "text_reply";

    public void createNotificationChannel(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            String description = context.getString(R.string.channel_description);

            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    private int notificationId = 0;
    private String sender = null;
    private String message = null;

    public void setNotificationContent(String sender, String message) {
        this.sender = sender;
        this.message = message;
        this.notificationId++;
    }

    final ArrayList<NotificationItem> notificationItems = new ArrayList<>();

    public void appendNotificationItem(String sender, String message) {
        NotificationItem item = new NotificationItem(sender, message, notificationItems.size());
        notificationItems.add(item);
    }

    public void showNotification(Context context, int id) {
        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationItem item;
        if(id == -1) {
            item = notificationItems.get(notificationItems.size() - 1);
        } else {
            item = notificationItems.get(id);
        }

        /* TODO REMOVE
        RemoteInput remoteInput = new RemoteInput.Builder(TEXT_REPLY)
                .setLabel(context.getString(R.string.stop))
                .build();
        */

        Intent replyIntent = new Intent(context, AlarmNotificationReceiver.class);
        replyIntent.putExtra("id", item.getId());

        PendingIntent replyPendingIntent =
                PendingIntent.getBroadcast(context,
                        item.getId(),
                        replyIntent,
                        PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Action action =
                new NotificationCompat.Action.Builder(R.drawable.ic_delete,
                        context.getString(R.string.stop), replyPendingIntent)
                        // TODO REMOVE.addRemoteInput(remoteInput)
                        .build();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setContentTitle(item.getSender())
                .setContentText(item.getMessage())
                .addAction(action)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(item.getId(), builder.build());
    }
}
