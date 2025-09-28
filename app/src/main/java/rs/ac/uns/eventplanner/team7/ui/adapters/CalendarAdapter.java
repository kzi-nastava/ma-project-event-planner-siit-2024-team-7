package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.BusynessDTO;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {

    public interface OnEventMoreInfoClickListener {
        void onEventClicked(Integer id);
    }

    private Context context;
    private List<BusynessDTO> events;

    @Setter
    private OnEventMoreInfoClickListener onEventMoreInfoClickListener;

    public CalendarAdapter(Context context, List<BusynessDTO> events) {
        this.context = context;
        this.events = events;
    }

    @Override
    public CalendarViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Inflate the layout for each item
        View view = LayoutInflater.from(context).inflate(R.layout.list_busyness_item, parent, false);
        return new CalendarViewHolder(view);
    }

    @Override
    public void onBindViewHolder(CalendarViewHolder holder, int position) {
        // Bind the data to the view
        BusynessDTO event = events.get(position);
        holder.nameView.setText(event.getName());
        holder.dateView.setText(event.getDate());
        holder.moreInfoButton.setOnClickListener(v -> onEventMoreInfoClickListener.onEventClicked(event.getId()));
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView dateView;
        MaterialButton moreInfoButton;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.item_name);
            dateView = itemView.findViewById(R.id.item_date);
            moreInfoButton = itemView.findViewById(R.id.item_event_details_btn);
        }
    }

    // Method to update the list
    public void updateEvents(List<BusynessDTO> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
}
