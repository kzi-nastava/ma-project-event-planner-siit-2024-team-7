package rs.ac.uns.eventplanner.team7.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import rs.ac.uns.eventplanner.team7.R;

public class SimpleCarouselAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_IMAGE = 0;
    private static final int VIEW_TYPE_BUTTON = 1;

    private int drawableResId;
    private int itemCount;

    public SimpleCarouselAdapter(int drawableResId, int itemCount) {
        this.drawableResId = drawableResId;
        this.itemCount = itemCount;
    }

    @Override
    public int getItemViewType(int position) {
        return position == itemCount - 1 ? VIEW_TYPE_BUTTON : VIEW_TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_BUTTON) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_button, parent, false);
            return new ButtonViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.normal_card, parent, false);
            return new ImageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder.getItemViewType() == VIEW_TYPE_IMAGE) {
            ImageViewHolder imageViewHolder = (ImageViewHolder) holder;
            imageViewHolder.eventImage.setImageResource(drawableResId);
        } else {
            ButtonViewHolder buttonViewHolder = (ButtonViewHolder) holder;
            buttonViewHolder.moreButton.setText("More...");
        }
    }

    @Override
    public int getItemCount() {
        return itemCount;
    }

    // ViewHolder for image cards
    public static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView eventImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            eventImage = itemView.findViewById(R.id.card_image);
        }
    }

    // ViewHolder for the "More..." button
    public static class ButtonViewHolder extends RecyclerView.ViewHolder {
        Button moreButton;

        public ButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            moreButton = itemView.findViewById(R.id.moreButton);
        }
    }
}
