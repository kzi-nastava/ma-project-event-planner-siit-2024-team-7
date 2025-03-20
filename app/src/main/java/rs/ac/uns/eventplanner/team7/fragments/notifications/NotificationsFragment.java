package rs.ac.uns.eventplanner.team7.fragments.notifications;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.NotificationAdapter;
import rs.ac.uns.eventplanner.team7.dto.notification.PersonalNotificationDTO;
import rs.ac.uns.eventplanner.team7.services.NotificationService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class NotificationsFragment extends Fragment {
    private RecyclerView notificationsView;
    private MaterialTextView messageTextView;
    private NotificationAdapter adapter;
    private final NotificationService notificationService = ClientUtils.injectService(NotificationService.class);

    public NotificationsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        messageTextView = view.findViewById(R.id.no_notifications_message_view);
        notificationsView = view.findViewById(R.id.notifications_recycler_view);
        notificationsView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new NotificationAdapter(requireContext(), new ArrayList<>());
        notificationsView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setContent();

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.notification_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            setContent();
        });
    }

    private void setContent() {
        messageTextView.setVisibility(View.VISIBLE);
        notificationsView.setVisibility(View.GONE);
        messageTextView.setText(R.string.fetching_data);
        adapter.clear();
        notificationService.getNotifications(JwtUtil.getAuthorizationValue(requireContext())).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<PersonalNotificationDTO>> call,
                                   @NonNull Response<List<PersonalNotificationDTO>> response) {
                if (!isAdded()) return;
                if (!response.isSuccessful()) {
                    messageTextView.setVisibility(View.VISIBLE);
                    notificationsView.setVisibility(View.GONE);
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                if (response.body() == null || response.body().isEmpty()) {
                    messageTextView.setVisibility(View.VISIBLE);
                    notificationsView.setVisibility(View.GONE);
                    messageTextView.setText(R.string.no_notifications_yet);
                } else {
                    messageTextView.setVisibility(View.GONE);
                    notificationsView.setVisibility(View.VISIBLE);
                    adapter.addAll(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<PersonalNotificationDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                messageTextView.setVisibility(View.VISIBLE);
                notificationsView.setVisibility(View.GONE);
                messageTextView.setText(R.string.unable_to_contact_server);
            }
        });
    }
}