package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
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

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.model.interfaces.BasicCard;
import rs.ac.uns.eventplanner.team7.model.interfaces.CardClickListener;
import rs.ac.uns.eventplanner.team7.model.interfaces.DetailedCard;

public class CardRecyclerViewAdapter<T extends BasicCard>
        extends RecyclerView.Adapter<CardRecyclerViewAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<T> data;
    @LayoutRes private int cardType = R.layout.horizontal_card;
    private final CardClickListener cardClickListener;

    public CardRecyclerViewAdapter(Context context, List<T> data, boolean useNormalCard,
                                   CardClickListener listener) {
        this.context = context;
        this.data = data;
        this.cardClickListener = listener;
        if (useNormalCard) this.cardType = R.layout.normal_card;
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

    public void addAll(@NonNull Collection<? extends T> itemsCollection) {
        int initialSize;
        synchronized (mutex) {
            initialSize = data.size();
            data.addAll(itemsCollection);
        }
        notifyItemRangeInserted(initialSize, itemsCollection.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(cardType, parent, false);
        return new ViewHolder((MaterialCardView) view, cardClickListener);
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
        private TextView descriptionView;
        private MaterialButton moreInfoButton;
        private final MaterialCardView cardView;
        private final CardClickListener cardClickListener;

        public ViewHolder(@NonNull MaterialCardView itemView, CardClickListener listener) {
            super(itemView);
            this.cardView = itemView;
            this.cardClickListener = listener;

            if (itemView.getId() == R.id.horizontal_card) {
                titleView = itemView.findViewById(R.id.horizontal_card_title);
                subtitleView = itemView.findViewById(R.id.horizontal_card_subtitle);
                imageView = itemView.findViewById(R.id.horizontal_card_image);
                return;
            }
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            imageView = itemView.findViewById(R.id.card_image);
            descriptionView = itemView.findViewById(R.id.card_description);
            moreInfoButton = itemView.findViewById(R.id.card_more_info_button);

        }

        public void bindData(BasicCard entity) {
            titleView.setText(entity.getTitle());
            subtitleView.setText(entity.getSubtitle());
            if (entity instanceof CardWithImage && imageView != null) {
                Picasso.get()
                        .load(((CardWithImage) entity).getCoverImage())
                        .placeholder(R.drawable.image_placeholder)
                        .error(R.drawable.image_placeholder)
                        .into(imageView);
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