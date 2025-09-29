package rs.ac.uns.eventplanner.team7.ui.fragments.notifications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.notification.PersonalNotificationDTO;

public class NotificationsMoreActionsFragment extends BottomSheetDialogFragment {

    public interface ActionClickListener {
        void OnMuteNotificationsClicked(boolean areNotificationsEnabled);
        void OnMarkAsReadClicked(@Nullable Integer notificationId);
        void OnDeleteClicked(@Nullable Integer notificationId);
    }

    private PersonalNotificationDTO notification;
    private MaterialButton markAsReadButton, deleteButton;
    private boolean notificationsEnabled;
    private ActionClickListener listener;

    public NotificationsMoreActionsFragment() {
        // Required empty public constructor
    }

    public static NotificationsMoreActionsFragment newInstance(@NonNull PersonalNotificationDTO notification,
                                                               @NonNull ActionClickListener listener) {
        NotificationsMoreActionsFragment fragment = new NotificationsMoreActionsFragment();
        fragment.notification = notification;
        fragment.listener = listener;
        return fragment;
    }

    public static NotificationsMoreActionsFragment newInstance(boolean notificationsEnabled,
                                                               @NonNull ActionClickListener listener) {
        NotificationsMoreActionsFragment fragment = new NotificationsMoreActionsFragment();
        fragment.notificationsEnabled = notificationsEnabled;
        fragment.listener = listener;
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications_more_actions, container, false);

        markAsReadButton = view.findViewById(R.id.mark_as_read_button);
        deleteButton = view.findViewById(R.id.delete_notification_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton closeButton = view.findViewById(R.id.more_info_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        if (notification != null) handleSingleNotificationActions();
        else handleGlobalActions(view);
    }

    private void handleSingleNotificationActions() {
        if (notification.isRead()) {
            markAsReadButton.setEnabled(false);
            markAsReadButton.setText(getString(R.string.already_read));
        } else {
            markAsReadButton.setOnClickListener(v -> {
                if (!notification.isRead()) listener.OnMarkAsReadClicked(notification.getId());
                dismiss();
            });
        }
        deleteButton.setOnClickListener(v -> {
            listener.OnDeleteClicked(notification.getId());
            dismiss();
        });
    }

    private void handleGlobalActions(@NonNull View view) {
        MaterialButton muteNotificationsButton = view.findViewById(R.id.mute_notifications_button);
        muteNotificationsButton.setVisibility(View.VISIBLE);
        if (!notificationsEnabled) muteNotificationsButton.setText(R.string.enable_notifications);
        muteNotificationsButton.setOnClickListener(v -> {
            listener.OnMuteNotificationsClicked(!notificationsEnabled);
            dismiss();
        });


        markAsReadButton.setText(getString(R.string.mark_all_as_read));
        markAsReadButton.setOnClickListener(v -> {
            listener.OnMarkAsReadClicked(null);
            dismiss();
        });

        deleteButton.setText(getString(R.string.delete_all_notifications));
        deleteButton.setOnClickListener(v -> {
            listener.OnDeleteClicked(null);
            dismiss();
        });
    }
}