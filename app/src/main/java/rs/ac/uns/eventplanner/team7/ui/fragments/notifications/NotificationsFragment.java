package rs.ac.uns.eventplanner.team7.ui.fragments.notifications;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.notification.PersonalNotificationDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UserPreferencesDTO;
import rs.ac.uns.eventplanner.team7.data.services.NotificationService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.NotificationAdapter;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;

public class NotificationsFragment extends Fragment implements
        NotificationsMoreActionsFragment.ActionClickListener {
    private final NotificationService notificationService = ClientUtils.injectService(NotificationService.class);
    private final UserService userService = ClientUtils.injectService(UserService.class);

    private RecyclerView notificationsView;
    private MaterialTextView messageTextView;
    private NotificationAdapter adapter;
    private final Page<PersonalNotificationDTO> page = Page.getDefault();
    private boolean hasShownFragment, notificationsEnabled = true, isLoading;
    private String bearerToken;

    private final Handler timeHandler = new Handler(Looper.getMainLooper());
    private final Runnable timeUpdater = new Runnable() {
        // For updating how long ago was the notification
        @Override
        public void run() {
            if (adapter != null) {
                int count = adapter.getItemCount();
                adapter.notifyItemRangeChanged(0, count);
            }
            timeHandler.postDelayed(this, 60_000);
        }
    };

    public NotificationsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        messageTextView = view.findViewById(R.id.no_notifications_message_view);
        notificationsView = view.findViewById(R.id.notifications_recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireContext().getSystemService(NotificationManager.class).cancelAll();

        bearerToken = JwtUtil.getAuthorizationValue(requireContext());

        adapter = new NotificationAdapter(
                requireContext(), new ArrayList<>(),
                c -> onCardClicked((PersonalNotificationDTO) c), this::onMoreActionsClicked);
        notificationsView.setAdapter(adapter);

        getNotificationEnabledStatus();
        setContent(false);

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.notification_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            setContent(false);
        });

        FloatingActionButton moreActionsButton = view.findViewById(R.id.more_actions_button);
        moreActionsButton.setOnClickListener(v -> onMoreActionsClicked(null));

        setupContentScrollListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        timeHandler.post(timeUpdater);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(
                        newNotificationReceiver,
                        new IntentFilter(WebSocketService.ACTION_NEW_NOTIFICATION)
                );
    }

    @Override
    public void onPause() {
        super.onPause();
        timeHandler.removeCallbacks(timeUpdater);
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(newNotificationReceiver);
    }

    @Override
    public void OnMuteNotificationsClicked(boolean areNotificationsEnabled) {
        Integer userId = JwtUtil.extractId(requireContext());
        userService.updatePreferences(bearerToken, userId, new UserPreferencesDTO(areNotificationsEnabled))
            .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<UserPreferencesDTO> call,
                                   @NonNull Response<UserPreferencesDTO> response) {
                notificationsEnabled = areNotificationsEnabled;
                if (!isAdded()) return;
                Snackbar.make(requireView(),"Preferences updated successfully", Snackbar.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(@NonNull Call<UserPreferencesDTO> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                Snackbar.make(requireView(), "Unable to update preferences", Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void OnMarkAsReadClicked(@Nullable Integer notificationId) {
        if (notificationId == null) handleServiceCall(notificationService.markAllAsRead(bearerToken));
        else handleServiceCall(notificationService.markAsRead(bearerToken, notificationId));
    }

    @Override
    public void OnDeleteClicked(@Nullable Integer notificationId) {
        if (notificationId == null) handleServiceCall(notificationService.deleteAll(bearerToken));
        else handleServiceCall(notificationService.deleteOne(bearerToken, notificationId));
    }

    private final BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.getSystemService(NotificationManager.class).cancelAll();
            setContent(false);
        }
    };

    private void setupContentScrollListener() {
        notificationsView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                var layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                int lastVisiblePosition = Objects.requireNonNull(layoutManager).findLastCompletelyVisibleItemPosition();
                if (lastVisiblePosition == adapter.getLastItemIndex() && !page.isLast())
                    onNextPage();
            }
        });
    }

    private void onNextPage() {
        if (isLoading || page.isLast()) return;
        page.nextPage();
        setContent(true);
    }

    private void getNotificationEnabledStatus() {
        final Integer userId = JwtUtil.extractId(requireContext());
        userService.getPreferences(bearerToken, userId).enqueue(
            new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<UserPreferencesDTO> call,
                                       @NonNull Response<UserPreferencesDTO> response) {
                    if (!isAdded() || response.body() == null) return;
                    notificationsEnabled = response.body().getNotificationsEnabled();
                }
                @Override
                public void onFailure(@NonNull Call<UserPreferencesDTO> call, @NonNull Throwable t) {}
            });
    }

    private void setContent(boolean isUpdate) {
        isLoading = true;
        if (!isUpdate) page.resetToDefault();
        notificationService.getNotifications(bearerToken, page.toQueryMapWithoutSort())
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Page<PersonalNotificationDTO>> call,
                                   @NonNull Response<Page<PersonalNotificationDTO>> response) {
                if (!isAdded()) return;
                if (!isUpdate) adapter.clear();
                if (!response.isSuccessful()) {
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                Page<PersonalNotificationDTO> pagedResponse = response.body();
                if (pagedResponse == null || pagedResponse.isEmpty()) {
                    if (page.isFirst()) {
                        messageTextView.setText(R.string.no_notifications_yet);
                    }
                    return;
                }
                page.update(pagedResponse);
                adapter.addAll(page.getContent());
                isLoading = false;
                messageTextView.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<Page<PersonalNotificationDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                messageTextView.setText(R.string.unable_to_contact_server);
                isLoading = false;
            }
        });
    }

    private void onCardClicked(PersonalNotificationDTO notification) {
        new MaterialAlertDialogBuilder(requireContext())
            .setTitle(String.format("%s\n%s", notification.getTitle(), notification.getSubtitle()))
            .setMessage(notification.getMessage())
            .setOnDismissListener(dialog -> {
                if (!notification.isRead()) {
                    handleServiceCall(notificationService.markAsRead(bearerToken,
                            notification.getId()));
                }
            })
            .setPositiveButton("Ok", (dialog, w) -> {
                if (!notification.isRead()) {
                    handleServiceCall(notificationService.markAsRead(bearerToken,
                            notification.getId()));
                }
                dialog.dismiss();
            })
            .setNegativeButton("Delete", (dialog, w) -> {
                handleServiceCall(notificationService.deleteOne(bearerToken,
                        notification.getId()));
                dialog.dismiss();
            })
            .show();
    }
    private void onMoreActionsClicked(PersonalNotificationDTO notification) {
        final String moreActionsFragmentTag = "notificationMoreActionsFragment";
        if (!hasShownFragment && getChildFragmentManager().findFragmentByTag(moreActionsFragmentTag) == null) {
            hasShownFragment = true;
            var moreInfoFragment = notification == null
                    ? NotificationsMoreActionsFragment.newInstance(notificationsEnabled, this)
                    : NotificationsMoreActionsFragment.newInstance(notification, this);
            moreInfoFragment.show(getChildFragmentManager(), moreActionsFragmentTag);
            getChildFragmentManager().registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                @Override
                public void onFragmentDetached(@NonNull FragmentManager fm, @NonNull Fragment f) {
                    if (f == moreInfoFragment) {
                        hasShownFragment = false;
                        getChildFragmentManager().unregisterFragmentLifecycleCallbacks(this);
                    }
                }
            }, false);
        }
    }

    private void handleServiceCall(Call<Void> serviceCall) {
        serviceCall.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    setContent(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                String message = t.getMessage();
                Log.e("Notifications", message == null ? "Unknown error" : message);
            }
        });
    }
}