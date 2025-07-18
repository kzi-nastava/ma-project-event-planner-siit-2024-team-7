package rs.ac.uns.eventplanner.team7.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.activities.HomeActivity;
import rs.ac.uns.eventplanner.team7.dto.notification.PersonalNotificationDTO;

public class NotificationUtils {
    public static final String CHANNEL_ID = "notification_channel";

    public static void createNotificationChannel(Context context) {
        CharSequence name = "General Notifications";
        int importance = NotificationManager.IMPORTANCE_HIGH; // High priority

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    public static void showNotification(Context context, PersonalNotificationDTO notification) {
        var builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(notification.getTitle())
                .setContentIntent(openNotificationsIntent(context))
                .addAction(new NotificationCompat.Action(
                        null,
                        context.getString(R.string.mark_as_read),
                        getMarkAsReadPendingIntent(context, notification.getId())))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(notification.getMessage()))
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(notification.getId(), builder.build());
    }

    private static PendingIntent openNotificationsIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("navigate_to", "notifications");

        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent getMarkAsReadPendingIntent(Context context, Integer id) {
        Intent markAsReadIntent = new Intent(context, NotificationActionReceiver.class);
        markAsReadIntent.setAction("MARK_AS_READ");
        markAsReadIntent.putExtra("NOTIFICATION_ID", id);
        markAsReadIntent.putExtra("TOKEN", JwtUtil.getAuthorizationValue(context));

        return PendingIntent.getBroadcast(context, id, markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
