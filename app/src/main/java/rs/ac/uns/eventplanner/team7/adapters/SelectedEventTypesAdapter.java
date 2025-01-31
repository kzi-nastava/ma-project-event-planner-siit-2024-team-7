package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.model.EventType;

public class SelectedEventTypesAdapter extends RecyclerView.Adapter<SelectedEventTypesAdapter.ViewHolder> {
    private final Context context;
    private final List<EventType> selectedEventTypes;

    public SelectedEventTypesAdapter(Context context, List<EventType> selectedEventTypes) {
        this.context = context;
        this.selectedEventTypes = selectedEventTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.horizontal_button_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EventType eventType = selectedEventTypes.get(position);
        holder.titleView.setText(eventType.getName());
        holder.subtitleView.setVisibility(View.GONE);
        holder.fabView.setOnClickListener(v -> {
            selectedEventTypes.remove(eventType);
            notifyDataSetChanged();
            holder.setVisibility(View.GONE);
        });
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return selectedEventTypes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        FloatingActionButton fabView;
        MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            fabView = itemView.findViewById(R.id.card_more_info_button);
            cardView = itemView.findViewById(R.id.horizontal_button_card);
        }

        public void setVisibility(int visibility) {
            cardView.setVisibility(visibility);
        }
    }
}
