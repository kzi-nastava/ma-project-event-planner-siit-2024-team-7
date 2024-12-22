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
import com.squareup.picasso.Picasso;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    private final Context context;
    private final List<?> items;
    private final String type; // Can be "events" or "items"

    public CarouselAdapter(Context context, List<?> items, String type) {
        this.context = context;
        this.items = items;
        this.type = type;
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
        if (type.equals("events")) {
            BasicEventDTO event = (BasicEventDTO) items.get(position);
            holder.titleView.setText(event.getName());
            holder.subtitleView.setText(event.getDate());
            holder.descriptionView.setText("");
            Picasso.get()
                    .load(event.getCoverImage())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.imageView);
        } else if (type.equals("items")) {
            BasicItemDTO item = (BasicItemDTO) items.get(position);
            holder.titleView.setText(item.getName());
            holder.subtitleView.setText(item.getType());
            holder.descriptionView.setText(String.valueOf(item.getPrice()));
            Picasso.get()
                    .load(item.getCoverImage())
                    .placeholder(R.drawable.image_placeholder)
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
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


