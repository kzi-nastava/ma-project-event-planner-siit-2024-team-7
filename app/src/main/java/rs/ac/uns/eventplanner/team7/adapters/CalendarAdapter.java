package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import android.util.Log;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.BusynessDTO;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.CalendarViewHolder> {
    private Context context;
    private List<BusynessDTO> events;

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
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    public static class CalendarViewHolder extends RecyclerView.ViewHolder {
        TextView nameView;
        TextView dateView;

        public CalendarViewHolder(View itemView) {
            super(itemView);
            nameView = itemView.findViewById(R.id.item_name);
            dateView = itemView.findViewById(R.id.item_date);
        }
    }

    // Method to update the list
    public void updateEvents(List<BusynessDTO> newEvents) {
        this.events = newEvents;
        notifyDataSetChanged();
    }
}
