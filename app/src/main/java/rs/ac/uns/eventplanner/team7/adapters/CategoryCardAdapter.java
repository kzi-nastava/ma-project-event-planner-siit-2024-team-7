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
import com.google.android.material.card.MaterialCardView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;

public class CategoryCardAdapter extends RecyclerView.Adapter<CategoryCardAdapter.ViewHolder> {
    private final Context context;
    private final List<CategoryResponseDTO> categories;

    public CategoryCardAdapter(Context context, List<CategoryResponseDTO> categories) {
        this.context = context;
        this.categories = categories;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.normal_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CategoryResponseDTO category = categories.get(position);
        holder.titleView.setText(category.getName());
        holder.subtitleView.setVisibility(View.GONE);
        holder.descriptionView.setText(category.getDescription());
        holder.imageView.setVisibility(View.GONE);
        holder.moreInfoButton.setText(R.string.edit);

        holder.moreInfoButton.setOnClickListener(v -> {

        });
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView titleView;
        TextView subtitleView;
        TextView descriptionView;
        MaterialButton moreInfoButton;
        MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.card_image);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            descriptionView = itemView.findViewById(R.id.card_description);
            moreInfoButton = itemView.findViewById(R.id.card_more_info_button);
            cardView = itemView.findViewById(R.id.normal_card);

        }
    }
}
