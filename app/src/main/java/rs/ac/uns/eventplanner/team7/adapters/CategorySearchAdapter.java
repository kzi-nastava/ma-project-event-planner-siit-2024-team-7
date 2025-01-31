package rs.ac.uns.eventplanner.team7.adapters;

import static android.view.View.VISIBLE;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.model.Category;

public class CategorySearchAdapter extends RecyclerView.Adapter<CategorySearchAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> categories;
    private final List<Category> selectedCategories;  // List of selected categories
    private final CategorySelectAdapter selectAdapter;  // Reference to the select adapter


    public CategorySearchAdapter(Context context, List<Category> categories, List<Category> selectedCategories, CategorySelectAdapter adapter) {
        this.context = context;
        this.categories = categories;
        this.selectedCategories = selectedCategories;
        this.selectAdapter = adapter;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_button_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);

        // Prevent showing categories already selected
        if (selectedCategories.contains(category)) {
            holder.itemView.setVisibility(View.GONE); // Hide if already selected
        } else {
            holder.itemView.setVisibility(VISIBLE);  // Show if not selected
        }

        holder.titleView.setVisibility(View.INVISIBLE);
        holder.subtitleView.setText(category.getName());
        Drawable icon = AppCompatResources.getDrawable(context, R.drawable.ic_add);
        holder.button.setIcon(icon);  // Add button

        holder.button.setOnClickListener(v -> {
            if (!selectedCategories.contains(category)) {
                selectedCategories.add(category);
                categories.remove(category);
                notifyDataSetChanged();
                selectAdapter.notifyDataSetChanged();
                holder.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public void updateData(List<Category> newCategories) {
        categories.clear();
        categories.addAll(newCategories);
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
