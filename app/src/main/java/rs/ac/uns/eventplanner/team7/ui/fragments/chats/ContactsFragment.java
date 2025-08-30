package rs.ac.uns.eventplanner.team7.ui.fragments.chats;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.services.ChatService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ContactAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;

public class ContactsFragment extends Fragment {

    private final ChatService chatService = ClientUtils.injectService(ChatService.class);
    private final UserService userService = ClientUtils.injectService(UserService.class);

    private RecyclerView contactsView;
    private MaterialTextView messageTextView;
    private ContactAdapter adapter;
    private String bearerToken;

    public ContactsFragment() {
    }

    public static ContactsFragment newInstance() {
        ContactsFragment fragment = new ContactsFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_contacts, container, false);
        messageTextView = view.findViewById(R.id.no_contacts_message_view);
        contactsView = view.findViewById(R.id.contacts_recycler_view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bearerToken = AuthUtil.getAuthorizationValue(requireContext());

        adapter = new ContactAdapter(requireContext(), new ArrayList<>(), this::onCardClicked);
        contactsView.setAdapter(adapter);

        setContent();

        SwipeRefreshLayout refreshLayout = view.findViewById(R.id.contact_swipe_refresh);
        refreshLayout.setOnRefreshListener(() -> {
            refreshLayout.setRefreshing(false);
            setContent();
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(
                        newNotificationReceiver,
                        new IntentFilter(WebSocketService.ACTION_NEW_NOTIFICATION)
                );
    }

    @Override
    public void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(requireContext())
                .unregisterReceiver(newNotificationReceiver);
    }

    private void onCardClicked(BasicCard contact) {
        userService.getUserDetails(bearerToken, contact.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(
                    @NonNull Call<GetUserDetailsResponseDTO> call,
                    @NonNull Response<GetUserDetailsResponseDTO> response
            ) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    Bundle bundle = new Bundle();
                    bundle.putParcelable("contactDTO", response.body());
                    Navigation.findNavController(requireView()).navigate(R.id.navigate_to_chat_from_contacts, bundle);
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

    private void setContent() {
        chatService.findContacts(bearerToken).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatContactDTO>> call,
                                   @NonNull Response<List<ChatContactDTO>> response) {
                if (!isAdded()) return;
                adapter.clear();
                if (response.body() == null || !response.isSuccessful()) {
                    messageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                adapter.addAll(response.body());
                messageTextView.setText(adapter.getItemCount() > 0 ? "" : getString(R.string.no_contacts_yet));
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatContactDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                messageTextView.setText(R.string.unable_to_contact_server);
            }
        });
    }

    private final BroadcastReceiver newNotificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            context.getSystemService(NotificationManager.class).cancelAll();
            setContent();
        }
    };
}