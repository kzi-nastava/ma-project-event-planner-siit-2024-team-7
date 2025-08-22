package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<ChatResponseDTO> chatMessages;
    private final String userProfilePicUrl;
    private final String loggedInUser;
    private OnMessagesInsertedListener listener;

    public ChatAdapter(Context context, List<ChatResponseDTO> chatMessages, String userProfilePicUrl) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.userProfilePicUrl = userProfilePicUrl;
        this.loggedInUser = AuthUtil.extractEmail(context);
    }

    @NonNull
    @Override
    public ChatAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_bubble, parent, false);
        return new ChatAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatAdapter.ViewHolder holder, int position) {
        ChatResponseDTO message = chatMessages.get(position);
        holder.bindData(context, message, this.userProfilePicUrl, this.loggedInUser);
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    public void setOnMessagesInsertedListener(OnMessagesInsertedListener listener) {
        this.listener = listener;
    }

    public void addAll(@NonNull Collection<ChatResponseDTO> contacts) {
        int initialSize;
        synchronized (mutex) {
            initialSize = chatMessages.size();
            chatMessages.addAll(contacts);
            Collections.reverse(chatMessages);
        }
        notifyItemRangeInserted(initialSize, contacts.size());

        if (listener != null)
            listener.onMessagesInserted(getItemCount());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = chatMessages.size();
            chatMessages.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout chatBubble;
        MaterialTextView messageTextView, timestampView;
        ImageView profilePic;

        public ViewHolder(View itemView) {
            super(itemView);
            chatBubble = itemView.findViewById(R.id.chat_bubble);
            messageTextView = itemView.findViewById(R.id.chat_message);
            timestampView = itemView.findViewById(R.id.chat_timestamp);
            profilePic = itemView.findViewById(R.id.chat_mini_pfp);
        }

        public void bindData(Context context, ChatResponseDTO chatResponseDTO, String photoUrl, String loggedInUser) {
            String isoTimestamp = chatResponseDTO.getTimestamp();

            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ISO_DATE_TIME;
                LocalDateTime dateTime = LocalDateTime.parse(isoTimestamp, inputFormatter);

                DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault());
                String formattedTime = dateTime.format(outputFormatter);

                timestampView.setText(formattedTime);
            } catch (Exception e) {
                timestampView.setText(isoTimestamp);
            }

            messageTextView.setText(chatResponseDTO.getMessage());

            if (photoUrl != null && !photoUrl.isEmpty()) {
                String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + photoUrl;
                Picasso.get()
                        .load(backendUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(profilePic);
            }
            if (!chatResponseDTO.getRecipientEmail().equals(loggedInUser)) {
                profilePic.setVisibility(View.GONE);

                LinearLayout.LayoutParams bubbleParams =
                        (LinearLayout.LayoutParams) chatBubble.getLayoutParams();
                bubbleParams.gravity = Gravity.END;
                chatBubble.setLayoutParams(bubbleParams);

                LinearLayout.LayoutParams timeParams =
                        (LinearLayout.LayoutParams) timestampView.getLayoutParams();
                timeParams.gravity = Gravity.END;
                timestampView.setLayoutParams(timeParams);

                messageTextView.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(
                                messageTextView.getContext(), R.color.blue_700))
                );
                messageTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.white)
                );
            }
            else {
                profilePic.setVisibility(View.VISIBLE);

                LinearLayout.LayoutParams bubbleParams =
                        (LinearLayout.LayoutParams) chatBubble.getLayoutParams();
                bubbleParams.gravity = Gravity.START;
                chatBubble.setLayoutParams(bubbleParams);

                LinearLayout.LayoutParams timeParams =
                        (LinearLayout.LayoutParams) timestampView.getLayoutParams();
                timeParams.gravity = Gravity.START;
                timestampView.setLayoutParams(timeParams);

                messageTextView.setBackgroundTintList(
                        ColorStateList.valueOf(ContextCompat.getColor(
                                messageTextView.getContext(), R.color.white))
                );
                messageTextView.setTextColor(
                        ContextCompat.getColor(context, R.color.black)
                );
            }
        }
    }

    public interface OnMessagesInsertedListener {
        void onMessagesInserted(int itemCount);
    }
}

