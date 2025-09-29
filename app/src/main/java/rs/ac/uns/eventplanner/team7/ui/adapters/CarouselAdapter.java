package rs.ac.uns.eventplanner.team7.ui.adapters;

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

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.data.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.data.interfaces.WithImage;
import rs.ac.uns.eventplanner.team7.utils.ImageLoader;

public class CarouselAdapter extends RecyclerView.Adapter<CarouselAdapter.ViewHolder> {
    private final Context context;
    private final List<BasicCard> items;

    @Setter
    private CardClickListener onMoreInfoClickListener;

    public CarouselAdapter(Context context, List<BasicCard> items) {
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.normal_image_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (holder.titleView == null || holder.subtitleView == null || holder.descriptionView == null || holder.imageView == null) {
            return;
        }
        holder.bindData(items.get(position));
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
        }

        public void bindData(BasicCard entity) {
            titleView.setText(entity.getTitle());
            subtitleView.setText(entity.getSubtitle());

            if (entity instanceof WithImage && imageView != null) {
                ImageLoader.loadImage(((WithImage) entity).getCoverImage(), imageView);
            }
            if (moreInfoButton == null || onMoreInfoClickListener == null) return;
            moreInfoButton.setOnClickListener(v -> onMoreInfoClickListener.onCardClicked(entity));
        }
    }
}


