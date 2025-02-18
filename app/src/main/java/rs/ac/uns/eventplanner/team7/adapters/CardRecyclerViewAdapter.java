package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.squareup.picasso.Picasso;

import java.util.Collection;
import java.util.List;

import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.DetailedCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.WithImage;

public class CardRecyclerViewAdapter<T extends BasicCard>
        extends RecyclerView.Adapter<CardRecyclerViewAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<T> data;
    @LayoutRes private final int cardType;
    private final CardClickListener cardClickListener;
    private String normalCardButtonText;
    private Drawable buttonImage;

    /// Horizontal card layout
    public CardRecyclerViewAdapter(Context context, List<T> data, CardClickListener listener) {
        this.context = context;
        this.data = data;
        this.cardClickListener = listener;
        this.cardType = R.layout.horizontal_card;
    }

    /// Horizontal card with button layout and optional custom button image
    public CardRecyclerViewAdapter(Context context, List<T> data, CardClickListener listener,
                                   Drawable buttonImage) {
        this.context = context;
        this.data = data;
        this.cardClickListener = listener;
        this.cardType = R.layout.horizontal_button_card;
        this.buttonImage = buttonImage;
    }

    /// Normal card layout with default text
    public CardRecyclerViewAdapter(Context context, List<T> data, CardClickListener listener, boolean withImage) {
        this.context = context;
        this.data = data;
        this.cardClickListener = listener;
        this.cardType = withImage ? R.layout.normal_image_card : R.layout.normal_card;
    }

    /// Normal card layout with custom text
    public CardRecyclerViewAdapter(Context context, List<T> data, CardClickListener listener,
                                   boolean withImage, String normalCardButtonText) {
        this.context = context;
        this.data = data;
        this.cardClickListener = listener;
        this.cardType = withImage ? R.layout.normal_image_card : R.layout.normal_card;
        this.normalCardButtonText = normalCardButtonText;
    }

    public void add(T item) {
        int position;
        synchronized (mutex) {
            data.add(item);
            position = getLastItemIndex();
        }
        notifyItemInserted(position);
    }

    public void addAll(@NonNull Collection<? extends T> itemsCollection) {
        int initialSize;
        synchronized (mutex) {
            initialSize = data.size();
            data.addAll(itemsCollection);
        }
        notifyItemRangeInserted(initialSize, itemsCollection.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = data.size();
            data.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public void remove(T entity) {
        int index = -1;
        synchronized (mutex) {
            for (int i = 0; i < data.size(); i++) {
                if (data.get(i).equals(entity)) {
                    index = i;
                    break;
                }
            }
            if (index > -1) data.remove(index);
        }
        if (index > -1) notifyItemRemoved(index);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(cardType, parent, false);
        return new ViewHolder((MaterialCardView) view, cardClickListener, normalCardButtonText, buttonImage);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T entity = data.get(position);
        holder.bindData(entity);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public int getLastItemIndex() {
        return getItemCount()-1;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleView;
        private final TextView subtitleView;
        private final ImageView imageView;
        private final TextView descriptionView;
        private final MaterialButton moreInfoButton;
        private final MaterialCardView cardView;
        private final CardClickListener cardClickListener;

        public ViewHolder(@NonNull MaterialCardView itemView, CardClickListener listener, String buttonText, Drawable buttonImage) {
            super(itemView);
            this.cardView = itemView;
            this.cardClickListener = listener;
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            imageView = itemView.findViewById(R.id.card_image);
            descriptionView = itemView.findViewById(R.id.card_description);
            moreInfoButton = itemView.findViewById(R.id.card_more_info_button);
            if (moreInfoButton != null) {
                if (buttonText != null) moreInfoButton.setText(buttonText);
                else if (buttonImage != null) moreInfoButton.setIcon(buttonImage);
            }
        }

        public void bindData(BasicCard entity) {
            titleView.setText(entity.getTitle());
            subtitleView.setText(entity.getSubtitle());
            if (entity instanceof WithImage && imageView != null) {
                String backendUrl = "http://" + BuildConfig.IP_ADDR + ":8080/api/images?imageUrl=" + ((WithImage) entity).getCoverImage();
                Picasso.get()
                        .load(backendUrl)
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(imageView);
            }
            if (entity instanceof BasicItemDTO && !((BasicItemDTO) entity).isCurrent()) {
                cardView.setBackgroundColor(Color.parseColor("#D3D3D3"));
            }
            if (entity instanceof DetailedCard && descriptionView != null) {
                DetailedCard detailedCard = (DetailedCard) entity;
                descriptionView.setText(detailedCard.getDescription());
            }
            if (moreInfoButton != null) {
                moreInfoButton.setOnClickListener(v -> cardClickListener.onCardClicked(entity));
            } else {
                cardView.setOnClickListener(v -> cardClickListener.onCardClicked(entity));
            }
        }
    }
}