package rs.ac.uns.eventplanner.team7.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SelectedEventTypesAdapter extends RecyclerView.Adapter<SelectedEventTypesAdapter.ViewHolder> {
    private final List<String> selectedEventTypes;

    public SelectedEventTypesAdapter(List<String> selectedEventTypes) {
        this.selectedEventTypes = selectedEventTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_1, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String eventType = selectedEventTypes.get(position);
        holder.textView.setText(eventType);
    }

    @Override
    public int getItemCount() {
        return selectedEventTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
