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

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {
    private final Context context;
    private final List<String> images;

    public ImageListAdapter(Context context, List<String> images) {
        this.context = context;
        this.images = images;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_button_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String image = images.get(position);
        holder.titleView.setText(image);
        holder.subtitleView.setVisibility(View.GONE);
        holder.fabView.setImageResource(R.drawable.baseline_cancel_24);  // Remove button

        holder.fabView.setOnClickListener(v -> {
            images.remove(image);
            notifyDataSetChanged();
            holder.setVisibility(View.GONE);
        });
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public void addImage(String imageName) {
        for (String image : images) {
            if (imageName == null || imageName.isEmpty() || image.equals(imageName))
                return;
        }
        images.add(imageName);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        FloatingActionButton fabView;
        MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.horizontal_card_title);
            subtitleView = itemView.findViewById(R.id.horizontal_card_subtitle);
            fabView = itemView.findViewById(R.id.horizontal_card_fab);
            cardView = itemView.findViewById(R.id.horizontal_button_card);
        }

        public void setVisibility(int visibility) {
            cardView.setVisibility(visibility);
        }
    }
}
