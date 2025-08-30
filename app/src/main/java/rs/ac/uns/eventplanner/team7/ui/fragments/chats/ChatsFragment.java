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
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.blocking.BlockUserRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetUserDetailsResponseDTO;
import rs.ac.uns.eventplanner.team7.data.services.ChatService;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.ui.adapters.ChatAdapter;
import rs.ac.uns.eventplanner.team7.ui.fragments.reporting.ReportReasonDialogFragment;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.WebSocketService;


public class ChatsFragment extends Fragment implements ContactInfoDialogFragment.OnActionClickListener {
    private final ChatService chatService = ClientUtils.injectService(ChatService.class);
    private final UserService userService = ClientUtils.injectService(UserService.class);
    private final ReportService reportService = ClientUtils.injectService(ReportService.class);

    private GetUserDetailsResponseDTO contactDTO;
    private MaterialTextView contactNameView, noMessageTextView;
    private RecyclerView chatsRecyclerView;
    private TextInputEditText messageInput;
    private LinearLayout mainContentView;
    private ContactInfoDialogFragment contactInfoDialog;

    private ChatAdapter adapter;
    private String bearerToken, email, contactName;
    private MaterialButton contactInfoButton;

    public ChatsFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            contactDTO = getArguments().getParcelable("contactDTO", GetUserDetailsResponseDTO.class);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chats, container, false);
        contactNameView = view.findViewById(R.id.chat_contact_name);
        noMessageTextView = view.findViewById(R.id.no_chats_message_view);
        chatsRecyclerView = view.findViewById(R.id.chats_recycler_view);
        mainContentView = view.findViewById(R.id.main_content_layout);
        messageInput = view.findViewById(R.id.chat_message_input);
        contactInfoButton = view.findViewById(R.id.contact_info_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mainContentView.setVisibility(View.INVISIBLE);
        bearerToken = AuthUtil.getAuthorizationValue(requireContext());
        email = AuthUtil.extractEmail(requireContext());
        adapter = new ChatAdapter(requireContext(), new ArrayList<>(), contactDTO.getPhotoURL());
        chatsRecyclerView.setAdapter(adapter);
        formatUserInfo();
        setContent();

        adapter.setOnMessagesInsertedListener(itemCount ->
                chatsRecyclerView.scrollToPosition(itemCount - 1)
        );

        adapter.setOnProfilePictureClickListener(this::showContactInfoDialog);

        view.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            view.getWindowVisibleDisplayFrame(r);

            int screenHeight = view.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            if (keypadHeight > screenHeight * 0.15) {
                int position = adapter.getItemCount() - 1;
                if (position > 0) chatsRecyclerView.smoothScrollToPosition(position);
            }
        });

        MaterialButton scrollToBottomButton = view.findViewById(R.id.scroll_to_bottom_button);

        chatsRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                RecyclerView.LayoutManager layoutManager = recyclerView.getLayoutManager();
                if (layoutManager instanceof LinearLayoutManager) {
                    LinearLayoutManager linearLayoutManager = (LinearLayoutManager) layoutManager;

                    int lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();
                    int totalItemCount = linearLayoutManager.getItemCount();

                    scrollToBottomButton.setVisibility(lastVisibleItemPosition == totalItemCount - 1 ? View.INVISIBLE : View.VISIBLE);
                }
            }
        });

        scrollToBottomButton.setOnClickListener(v ->
                chatsRecyclerView.smoothScrollToPosition(adapter.getItemCount() - 1)
        );

        TextInputLayout sendLayout = view.findViewById(R.id.chat_message_input_layout);
        sendLayout.setEndIconOnClickListener(v -> sendMessage());

        contactInfoButton.setOnClickListener(v -> showContactInfoDialog());
    }

    private void showContactInfoDialog() {
        if (contactInfoDialog != null) {
            contactInfoDialog.dismiss();
        }
        contactInfoDialog = ContactInfoDialogFragment.newInstance(contactName, contactDTO.getPhotoURL(), contactDTO.getRole());
        contactInfoDialog.show(getChildFragmentManager(), "ContactInfoDialog");
        contactInfoDialog.setOnActionClickListener(this);
    }

    @Override
    public void OnBlockUserClicked() {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.block_user)
                .setMessage(getString(R.string.confirm_blocking, contactName))
                .setPositiveButton(R.string.confirm, (dialog, which) -> blockUser())
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss())
                .setCancelable(false)
                .show();
    }

    @Override
    public void OnReportUserClicked() {
        var dialog = ReportReasonDialogFragment.newInstance(true);
        dialog.setCancelable(false);
        dialog.setOnSubmitClickListener(reportReason -> {
            String userEmail = AuthUtil.extractEmail(requireContext());
            CreateReportRequestDTO dto = new CreateReportRequestDTO(userEmail, contactDTO.getEmail(), reportReason);
            reportService.create(bearerToken, dto).enqueue(new Callback<>() {
                @Override
                public void onResponse(
                        @NonNull Call<ReportDTO> call,
                        @NonNull Response<ReportDTO> response
                ) {
                    if (!isAdded()) return;
                    View view = getView();
                    Context context = getContext();
                    if (response.isSuccessful() && response.body() != null) {
                        if (view == null) return;
                        Snackbar.make(view, R.string.report_submitted, Snackbar.LENGTH_SHORT).show();
                        return;
                    }
                    if (response.code() == 400) {
                        var errorDto = ClientUtils.convertToErrorMessage(response.errorBody());
                        if (errorDto != null && view != null && context != null) {
                            Snackbar.make(view, errorDto.getMessage(), Snackbar.LENGTH_SHORT).show();
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ReportDTO> call, @NonNull Throwable t) {
                    String message = t.getMessage();
                    if (message != null) Log.d("ERROR", message);
                }
            });
        });
        dialog.show(getChildFragmentManager(), "ReportUserDialog");
    }

    private void blockUser() {
        contactInfoButton.setEnabled(false);
        BlockUserRequestDTO dto = new BlockUserRequestDTO(email, contactDTO.getEmail());
        userService.blockUser(bearerToken, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    Toast.makeText(requireContext(), R.string.user_blocked_successfully, Toast.LENGTH_SHORT).show();
                    Navigation.findNavController(requireView()).navigate(R.id.navigate_back_to_contacts_after_blocking);
                    return;
                }
                contactInfoButton.setEnabled(true);
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                contactInfoButton.setEnabled(true);
                String message = t.getMessage();
                if (message == null) return;
                Log.d("ERROR", message);
            }
        });
    }

    private void sendMessage() {
        messageInput.clearFocus();
        String message = Objects.requireNonNull(messageInput.getText()).toString();
        if (message.isEmpty()) return;
        ChatRequestDTO dto = new ChatRequestDTO(contactDTO.getEmail(), message);
        chatService.sendChatMessage(bearerToken, dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ChatResponseDTO> call,
                                   @NonNull Response<ChatResponseDTO> response) {
                if (!isAdded()) return;
                if (response.body() == null || !response.isSuccessful()) return;
                adapter.add(response.body());
            }

            @Override
            public void onFailure(@NonNull Call<ChatResponseDTO> call, @NonNull Throwable t) {
                Log.d("ERROR", Objects.requireNonNull(t.getMessage()));
            }
        });
        messageInput.setText("");
    }

    private void formatUserInfo() {
        if (contactDTO.getFirstName() != null && contactDTO.getLastName() != null) {
            contactName = String.format("%s %s", contactDTO.getFirstName(), contactDTO.getLastName());
        } else if (contactDTO.getOrgName() != null) {
            contactName = contactDTO.getOrgName();
        } else {
            contactName = contactDTO.getEmail();
        }
        contactNameView.setText(contactName);
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
        chatService.findBySenderAndRecipient(bearerToken, contactDTO.getId()).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<List<ChatResponseDTO>> call,
                                   @NonNull Response<List<ChatResponseDTO>> response) {
                if (!isAdded()) return;
                adapter.clear();
                boolean isSuccess = response.isSuccessful();
                noMessageTextView.setVisibility(isSuccess ? View.GONE : View.VISIBLE);
                mainContentView.setVisibility(View.VISIBLE);
                if (response.body() == null || !isSuccess) {
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
                noMessageTextView.setVisibility(View.VISIBLE);
                mainContentView.setVisibility(View.INVISIBLE);
            }
        });

        chatService.markAsRead(bearerToken, contactDTO.getId()).enqueue(new Callback<>() {
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