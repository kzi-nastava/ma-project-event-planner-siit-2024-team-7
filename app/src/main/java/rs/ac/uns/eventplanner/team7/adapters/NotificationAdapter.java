package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.notification.PersonalNotificationDTO;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<PersonalNotificationDTO> notifications;

    public NotificationAdapter(Context context, List<PersonalNotificationDTO> notifications) {
        this.context = context;
        this.notifications = notifications;
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
        holder.titleTextView.setText(notification.getTitle());
        LocalDateTime dateTime = LocalDateTime.parse(notification.getTimestamp());
        holder.timeStampTextView.setText(dateTime.format(DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm")));
    }

    @Override
    public int getItemCount() {
        return notifications.size();
    }

    public void addAll(@NonNull Collection<PersonalNotificationDTO> dtos) {
        int initialSize;
        synchronized (mutex) {
            initialSize = notifications.size();
            notifications.addAll(dtos);
        }
        notifyItemRangeInserted(initialSize, dtos.size());
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
        MaterialTextView titleTextView, timeStampTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.notification_title);
            timeStampTextView = itemView.findViewById(R.id.notification_timestamp);
        }
    }
}
