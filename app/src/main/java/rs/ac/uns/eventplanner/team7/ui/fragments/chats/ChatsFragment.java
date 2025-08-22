package rs.ac.uns.eventplanner.team7.ui.fragments.chats;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.ChatService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ChatAdapter;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;


public class ChatsFragment extends Fragment {

    private final ChatService chatService = ClientUtils.injectService(ChatService.class);

    private ChatContactDTO contactDTO;
    private ImageView profilePicView, sendButton;
    private MaterialTextView contactNameView, noMessageTextView;
    private RecyclerView chatsRecycleView;
    private TextInputEditText messageInput;

    private ChatAdapter adapter;
    private String bearerToken;

    public ChatsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactDTO = getArguments().getParcelable("contactDTO", ChatContactDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        profilePicView = view.findViewById(R.id.chat_profile_pic);
        contactNameView = view.findViewById(R.id.chat_contact_name);
        noMessageTextView = view.findViewById(R.id.no_chats_message_view);
        chatsRecycleView = view.findViewById(R.id.chats_recycler_view);
        messageInput = view.findViewById(R.id.chat_message_input);
        sendButton = view.findViewById(R.id.send_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        adapter = new ChatAdapter(requireContext(), new ArrayList<>(), contactDTO.getPhotoUrl());
        chatsRecycleView.setAdapter(adapter);

        if (contactDTO.getPhotoUrl() != null && !contactDTO.getPhotoUrl().isEmpty()) {
            String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + contactDTO.getPhotoUrl();
            Picasso.get()
                    .load(backendUrl)
                    .placeholder(R.drawable.image_placeholder)
                    .error(R.drawable.image_placeholder)
                    .into(profilePicView);
        }
        contactNameView.setText(contactDTO.getUserEmail());

        setContent();

        adapter.setOnMessagesInsertedListener(itemCount ->
                chatsRecycleView.scrollToPosition(itemCount - 1)
        );

        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);

            int screenHeight = view.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                chatsRecycleView.smoothScrollToPosition(adapter.getItemCount() - 1);
            }
        });

        sendButton.setOnClickListener(v -> {
            String message = Objects.requireNonNull(messageInput.getText()).toString();
            if (message.isEmpty()) return;
            ChatRequestDTO dto = new ChatRequestDTO(contactDTO.getUserEmail(), message);
            chatService.sendChatMessage(bearerToken, dto).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ChatResponseDTO> call,
                                       @NonNull Response<ChatResponseDTO> response) {
                    if (!isAdded()) return;
                    if (response.body() == null || !response.isSuccessful()) return;
                    setContent();
                }

                @Override
                public void onFailure(@NonNull Call<ChatResponseDTO> call, @NonNull Throwable t) {
                    Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                }
            });
            messageInput.setText("");

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

    private void setContent() {
        chatService.findBySenderAndRecipient(bearerToken, contactDTO.getUserId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatResponseDTO>> call,
                                   @NonNull Response<List<ChatResponseDTO>> response) {
                if (!isAdded()) return;
                adapter.clear();
                if (response.body() == null || !response.isSuccessful()) {
                    noMessageTextView.setText(R.string.unable_to_contact_server);
                    return;
                }
                adapter.addAll(response.body());
                noMessageTextView.setText("");
            }

            @Override
            public void onFailure(@NonNull Call<List<ChatResponseDTO>> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
                noMessageTextView.setText("");
            }
        });

        chatService.markAsRead(bearerToken, contactDTO.getUserId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call,
                                   @NonNull Response<Void> response) {
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
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