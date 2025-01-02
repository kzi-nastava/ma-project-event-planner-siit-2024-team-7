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
import com.squareup.picasso.Picasso;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.BasicCard;
import rs.ac.uns.eventplanner.team7.dto.DetailedCard;

public class HomeRecyclerViewAdapter<T extends BasicCard>
        extends RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder> {

    private final Context context;
    private final List<T> data;
    @LayoutRes private int cardType = R.layout.horizontal_card;

    public HomeRecyclerViewAdapter(Context context, List<T> data, boolean useNormalCard) {
        this.context = context;
        this.data = data;
        if (useNormalCard) this.cardType = R.layout.normal_card;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(cardType, parent, false);
        return new ViewHolder(view, cardType == R.layout.normal_card);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T entity = data.get(position);
        Picasso.get()
                .load(entity.getCoverImage())
                .placeholder(R.drawable.image_placeholder)
                .into(holder.imageView);
        holder.titleView.setText(entity.getTitle());
        holder.subtitleView.setText(entity.getSubtitle());
        holder.setOnClickListener(entity.getId());
        if (cardType == R.layout.normal_card) {
            holder.descriptionView.setText(((DetailedCard) entity).getDescription());
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView titleView;
        TextView subtitleView;
        ImageView imageView;
        TextView descriptionView; // on normal card
        MaterialButton moreInfoButton;

        public ViewHolder(@NonNull View itemView, boolean useNormalCard) {
            super(itemView);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            if (useNormalCard) {
                imageView = itemView.findViewById(R.id.card_image);
                descriptionView = itemView.findViewById(R.id.card_description);
            } else {
                imageView = itemView.findViewById(R.id.horizontal_card_image);
            }
            moreInfoButton = itemView.findViewById(R.id.card_more_info_button);
        }

        public void setOnClickListener(Integer id) {
            moreInfoButton.setOnClickListener(v -> {
                // TODO open info page
            });
        }
    }
}
