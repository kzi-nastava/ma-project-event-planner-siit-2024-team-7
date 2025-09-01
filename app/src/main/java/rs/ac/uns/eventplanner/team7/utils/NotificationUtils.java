package rs.ac.uns.eventplanner.team7.utils;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.Navigation;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.activities.HomeActivity;
import rs.ac.uns.eventplanner.team7.data.dto.notification.PersonalNotificationDTO;

public class NotificationUtils {
    public static final String CHANNEL_ID = "notification_channel";
    public static final String CHAT_CHANNEL_ID = "chat_channel";

    private static final UserService userService = ClientUtils.injectService(UserService.class);

    public static void createNotificationChannel(Context context) {
        CharSequence name = "General Notifications";
        CharSequence chat = "Messages";
        int importance = NotificationManager.IMPORTANCE_HIGH; // High priority

        NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
        NotificationChannel chatChannel = new NotificationChannel(CHAT_CHANNEL_ID, chat, importance);

        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        if (notificationManager != null) {
            notificationManager.createNotificationChannel(channel);
            notificationManager.createNotificationChannel(chatChannel);
        }
    }

    public static String getChannelId() {
        return CHANNEL_ID;
    }

    public static String getChatChannelId() {
        return CHAT_CHANNEL_ID;
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

    public static void showChatNotification(Context context, ChatResponseDTO chatDTO) {
        userService.getUserDetails(chatDTO.getSenderId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<GetUserDetailsResponseDTO> call,
                    @NonNull Response<GetUserDetailsResponseDTO> response
            ) {
                if (response.isSuccessful() && response.body() != null) {
                    var builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.mipmap.ic_launcher_round)
                            .setContentTitle(chatDTO.getSenderEmail())
                            .setContentIntent(openChatsIntent(context, response.body()))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(chatDTO.getMessage()))
                            .setAutoCancel(true);

                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    notificationManager.notify(chatDTO.getId(), builder.build());
                }
            }

            @Override
            public void onFailure(
                    @NonNull Call<GetUserDetailsResponseDTO> call,
                    @NonNull Throwable t
            ) {
                String message = t.getMessage();
                if (message == null) return;
                Log.d("ERROR", message);
            }
        });
    }

    private static PendingIntent openNotificationsIntent(Context context) {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("navigate_to", "notifications");

        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent openChatsIntent(Context context, GetUserDetailsResponseDTO dto) {

        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("navigate_to", "chats");
        Bundle bundle = new Bundle();
        bundle.putParcelable("contactDTO", dto);
        intent.putExtra("message", bundle);

        return PendingIntent.getActivity(context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    private static PendingIntent getMarkAsReadPendingIntent(Context context, Integer id) {
        Intent markAsReadIntent = new Intent(context, NotificationActionReceiver.class);
        markAsReadIntent.setAction("MARK_AS_READ");
        markAsReadIntent.putExtra("NOTIFICATION_ID", id);
        markAsReadIntent.putExtra("TOKEN", AuthUtil.getAuthorizationValue(context));

        return PendingIntent.getBroadcast(context, id, markAsReadIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }

    public static void showNotificationWithPDF(Context context, String title, String message, Uri fileUri) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, "application/pdf");
        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        notificationManager.notify(1, builder.build());
    }
}
