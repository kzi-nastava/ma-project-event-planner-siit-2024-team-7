package rs.ac.uns.eventplanner.team7.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.notification.PersonalNotificationDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {
    public interface MoreActionsClickListener {
        void onClicked(PersonalNotificationDTO notification);
    }

    private final Object mutex = new Object();
    private final Context context;
    private final List<PersonalNotificationDTO> notifications;
    private final CardClickListener cardClickListener;
    private final MoreActionsClickListener moreActionsClickListener;

    public NotificationAdapter(Context context, List<PersonalNotificationDTO> notifications,
                               CardClickListener cardClickListener,
                               MoreActionsClickListener moreActionsClickListener) {
        this.context = context;
        this.notifications = notifications;
        this.cardClickListener = cardClickListener;
        this.moreActionsClickListener = moreActionsClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.notification_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PersonalNotificationDTO notification = notifications.get(position);
        holder.bindData(notification);
        holder.moreInfoButton.setOnClickListener(v ->
                moreActionsClickListener.onClicked(notification));
        holder.itemView.setOnClickListener(v ->
                cardClickListener.onCardClicked(notification));
    }

    public int getLastItemIndex() {
        return getItemCount()-1;
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void addAll(@NonNull Collection<PersonalNotificationDTO> notifications) {
        int initialSize;
        synchronized (mutex) {
            initialSize = this.notifications.size();
            this.notifications.addAll(notifications);
        }
        notifyItemRangeInserted(initialSize, notifications.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = notifications.size();
            notifications.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleTextView, timeStampTextView, shortMessageTextView;
        ImageView unreadIcon;
        MaterialButton moreInfoButton;

        public ViewHolder(View itemView) {
            super(itemView);
            unreadIcon = itemView.findViewById(R.id.notification_unread_icon);
            titleTextView = itemView.findViewById(R.id.notification_title);
            timeStampTextView = itemView.findViewById(R.id.notification_timestamp);
            shortMessageTextView = itemView.findViewById(R.id.notification_message_short);
            moreInfoButton = itemView.findViewById(R.id.notification_more_info_button);
        }

        public void bindData(PersonalNotificationDTO notification) {
            if (notification.isRead()) {
                unreadIcon.setVisibility(View.GONE);
            }

            titleTextView.setText(notification.getTitle());
            String timeDifference = getTimeDifference(notification.getTimestamp());
            timeStampTextView.setText(timeDifference);
            shortMessageTextView.setText(notification.getMessage());
        }

        private String getTimeDifference(String timestamp) {
            LocalDateTime now = LocalDateTime.now(), dateTime = LocalDateTime.parse(timestamp);

            final var defaultLocale = Locale.getDefault();

            long seconds = Duration.between(dateTime, now).getSeconds();
            if (seconds < 60) return "<1mi";

            long minutes = seconds / 60;
            if (minutes < 60) return String.format(defaultLocale, "%dmi", minutes);

            long hours = minutes / 60;
            if (hours < 24) return String.format(defaultLocale, "%dh", hours);

            long days = hours / 24;
            if (days < 31) return String.format(defaultLocale, "%dd", days);

            long months = days / 30;
            if (months < 12) return String.format(defaultLocale, "%dmo", months);

            long years = months / 12;
            return String.format(defaultLocale, "%dy", years);
        }
    }
}
