package rs.ac.uns.eventplanner.team7.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import android.Manifest;

import rs.ac.uns.eventplanner.team7.R;

public class NotificationUtils {
    private static final String CHANNEL_ID = "notification_channel";

    public static void createNotificationChannel(Context context) {
        CharSequence name = "General Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH; // High priority

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static String getChannelId() {
        return CHANNEL_ID;
    }

    public static void showNotification(Context context, String title, String message) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, NotificationUtils.getChannelId())
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
