package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import okhttp3.MultipartBody;
import rs.ac.uns.eventplanner.team7.R;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.ViewHolder> {
    private final Context context;
    private final List<String> imageNames;
    private final List<MultipartBody.Part> images;

    public ImageListAdapter(Context context, List<String> imageNames, List<MultipartBody.Part> images) {
        this.context = context;
        this.imageNames = imageNames;
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
        String image = imageNames.get(position);
        holder.titleView.setText(image);
        holder.subtitleView.setVisibility(View.GONE);
        holder.button.setOnClickListener(v -> {
            imageNames.remove(image);
            notifyDataSetChanged();
            holder.setVisibility(View.GONE);
        });
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return imageNames.size();
    }

    public void addImage(String imageName, MultipartBody.Part image) {
        for (String url : imageNames) {
            if (imageName == null || imageName.isEmpty() || url.equals(imageName))
                return;
        }
        imageNames.add(imageName);
        images.add(image);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        MaterialButton button;
        MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            button = itemView.findViewById(R.id.card_more_info_button);
            cardView = itemView.findViewById(R.id.horizontal_button_card);
        }

        public void setVisibility(int visibility) {
            cardView.setVisibility(visibility);
        }
    }
}
