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
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.BaseViewHolder> {

    private static final int VIEW_TYPE_MESSAGE = 0;
    private static final int VIEW_TYPE_DATE_HEADER = 1;

    private final Object mutex = new Object();
    private final Context context;
    private final List<ChatResponseDTO> chatMessages;
    private final List<Object> items = new ArrayList<>();
    private final String userProfilePicUrl;
    private final String loggedInUser;

    @Setter
    private OnMessagesInsertedListener onMessagesInsertedListener;

    @Setter
    private OnProfilePictureClickListener onProfilePictureClickListener;

    public ChatAdapter(Context context, List<ChatResponseDTO> chatMessages, String userProfilePicUrl) {
        this.context = context;
        this.chatMessages = chatMessages;
        this.userProfilePicUrl = userProfilePicUrl;
        this.loggedInUser = AuthUtil.extractEmail(context);
        rebuildItems();
    }

    @Override
    public int getItemViewType(int position) {
        return (items.get(position) instanceof DateHeaderItem)
                ? VIEW_TYPE_DATE_HEADER
                : VIEW_TYPE_MESSAGE;
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == VIEW_TYPE_DATE_HEADER) {
            View v = inflater.inflate(R.layout.chat_date_header, parent, false);
            return new DateHeaderViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.chat_bubble, parent, false);
            return new MessageViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        Object item = items.get(position);
        if (holder instanceof MessageViewHolder) {
            ((MessageViewHolder) holder).bindData(
                    context, (ChatResponseDTO) item, this.userProfilePicUrl, this.loggedInUser,
                    onProfilePictureClickListener
            );
        } else if (holder instanceof DateHeaderViewHolder) {
            ((DateHeaderViewHolder) holder).bind(((DateHeaderItem) item).label);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void add(@NonNull ChatResponseDTO message) {
        int position;
        synchronized (mutex) {
            chatMessages.add(message);
            rebuildItems();
            position = getItemCount() - 1;
        }
        notifyItemInserted(position);
        if (onMessagesInsertedListener != null) {
            onMessagesInsertedListener.onMessagesInserted(getItemCount());
        }
    }

    public void addAll(@NonNull Collection<ChatResponseDTO> messages) {
        int initialSize;
        synchronized (mutex) {
            initialSize = items.size();
            chatMessages.addAll(messages);
            rebuildItems();
        }

        notifyItemRangeInserted(initialSize, items.size());
        if (onMessagesInsertedListener != null) {
            onMessagesInsertedListener.onMessagesInserted(getItemCount());
        }
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = items.size();
            chatMessages.clear();
            items.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    private void rebuildItems() {
        items.clear();
        LocalDate lastDate = null;

        for (ChatResponseDTO chatMessage : chatMessages) {
            LocalDateTime dateTime = parseDate(chatMessage.getTimestamp());
            LocalDate currentDate = (dateTime != null) ? dateTime.toLocalDate() : null;

            if (currentDate != null && (!currentDate.equals(lastDate))) {
                items.add(new DateHeaderItem(formatDateLabel(currentDate)));
                lastDate = currentDate;
            }
            items.add(chatMessage);
        }
    }

    @Nullable
    private LocalDateTime parseDate(String date) {
        try {
            return OffsetDateTime.parse(date).toLocalDateTime();
        } catch (Exception ignore) {
            try { return LocalDateTime.parse(date, DateTimeFormatter.ISO_DATE_TIME); }
            catch (Exception e) { return null; }
        }
    }

    private String formatDateLabel(LocalDate date) {
        LocalDate today = LocalDate.now();
        if (date.equals(today)) return context.getString(R.string.today);
        if (date.equals(today.minusDays(1))) return context.getString(R.string.yesterday);
        String format = today.getYear() == date.getYear() ? "MMM dd" : "MMM dd, yyyy";
        return date.format(DateTimeFormatter.ofPattern(format, Locale.getDefault()));
    }

    private String formatTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault()));
    }

    public static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        public BaseViewHolder(@NonNull View itemView) { super(itemView); }
    }

    public static class DateHeaderViewHolder extends BaseViewHolder {
        MaterialTextView dateHeader;
        public DateHeaderViewHolder(View itemView) {
            super(itemView);
            dateHeader = itemView.findViewById(R.id.date_header);
        }
        void bind(String label) {
            dateHeader.setText(label);
        }
    }

    public class MessageViewHolder extends BaseViewHolder {
        LinearLayout chatBubble;
        MaterialTextView messageTextView, timestampView;
        ImageView profilePic;
        MaterialCardView profilePicWrapper;

        public MessageViewHolder(View itemView) {
            super(itemView);
            chatBubble = itemView.findViewById(R.id.chat_bubble);
            messageTextView = itemView.findViewById(R.id.chat_message);
            timestampView = itemView.findViewById(R.id.chat_timestamp);
            profilePic = itemView.findViewById(R.id.chat_mini_pfp);
            profilePicWrapper = itemView.findViewById(R.id.chat_pfp_wrapper);
        }

        public void bindData(Context context, ChatResponseDTO chatResponseDTO,
                             String photoUrl, String loggedInUser,
                             OnProfilePictureClickListener onProfileClick) {

            String isoTimestamp = chatResponseDTO.getTimestamp();
            LocalDateTime dt = parseDate(isoTimestamp);
            timestampView.setText(dt != null ? formatTime(dt) : isoTimestamp);

            messageTextView.setText(chatResponseDTO.getMessage());

            if (photoUrl != null && !photoUrl.isEmpty()) {
                String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + photoUrl;
                Picasso.get()
                        .load(backendUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(profilePic);
            }

            boolean isOutgoing = !chatResponseDTO.getRecipientEmail().equals(loggedInUser);

            if (isOutgoing) {
                profilePicWrapper.setVisibility(View.GONE);

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
                messageTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
            } else {
                profilePicWrapper.setVisibility(View.VISIBLE);
                profilePicWrapper.setOnClickListener(v -> {
                    if (onProfileClick != null) onProfileClick.onProfilePictureClicked();
                });

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
                messageTextView.setTextColor(ContextCompat.getColor(context, R.color.black));
            }
        }
    }

    static class DateHeaderItem {
        final String label;
        DateHeaderItem(String label) { this.label = label; }
    }

    public interface OnMessagesInsertedListener {
        void onMessagesInserted(int itemCount);
    }
    public interface OnProfilePictureClickListener {
        void onProfilePictureClicked();
    }
}

