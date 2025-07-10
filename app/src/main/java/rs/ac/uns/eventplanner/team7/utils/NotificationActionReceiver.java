package rs.ac.uns.eventplanner.team7.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationManagerCompat;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.services.NotificationService;

public class NotificationActionReceiver extends BroadcastReceiver {

    private static final String TAG = "MarkAsReadReceiver";
    private final NotificationService notificationService = ClientUtils.injectService(NotificationService.class);

    @Override
    public void onReceive(Context context, Intent intent) {
        if ("MARK_AS_READ".equals(intent.getAction())) {
            int notificationId = intent.getIntExtra("NOTIFICATION_ID", -1);
            if (notificationId != -1) {
                try {
                    String token = JwtUtil.getAuthorizationValue(context);
                    notificationService.markAsRead(token, notificationId).enqueue(new Callback<>() {
                        @Override
                        public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                            NotificationManagerCompat.from(context).cancel(notificationId);
                            Log.d(TAG, "Marked notification as read");
                        }

                        @Override
                        public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                            String errorMessage = t.getMessage();
                            Log.e(TAG, errorMessage == null ? "Mark as read request failed" : errorMessage);
                        }
                    });
                } catch (Exception ex) {
                    String errorMessage = ex.getMessage();
                    Log.e(TAG, errorMessage == null ? "Unknown error" : errorMessage);
                }
            }
        }
    }
}
