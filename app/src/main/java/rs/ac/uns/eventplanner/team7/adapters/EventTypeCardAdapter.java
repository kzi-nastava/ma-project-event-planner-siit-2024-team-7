package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;

public class EventTypeCardAdapter extends RecyclerView.Adapter<EventTypeCardAdapter.ViewHolder> {
    private final Context context;
    private final List<GetEventTypeResponseDTO> eventTypes;

    public EventTypeCardAdapter(Context context, List<GetEventTypeResponseDTO> eventTypes) {
        this.context = context;
        this.eventTypes = eventTypes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.normal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.titleView == null || holder.subtitleView == null || holder.descriptionView == null || holder.imageView == null) {
            return;
        }
        GetEventTypeResponseDTO dto = eventTypes.get(position);
        holder.titleView.setText(dto.getName());
        holder.subtitleView.setVisibility(View.GONE);
        holder.descriptionView.setText(dto.getDescription());
        holder.imageView.setVisibility(View.GONE);
        holder.moreInfoButton.setText("Update");
    }


    @Override
    public int getItemCount() {
        return eventTypes.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView subtitleView;
        TextView descriptionView;
        MaterialButton moreInfoButton;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            descriptionView = itemView.findViewById(R.id.card_description);
            moreInfoButton = itemView.findViewById(R.id.card_more_info_button);

            // Optionally, set a listener for the button
            if (moreInfoButton == null) { //happens if there is no fav item/event
                return;
            }
            moreInfoButton.setOnClickListener(v -> {
                // TODO: redirect to event details/item details page
            });
        }
    }
}
