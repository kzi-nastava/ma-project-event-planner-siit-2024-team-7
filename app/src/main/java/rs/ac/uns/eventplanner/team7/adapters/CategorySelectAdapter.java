package rs.ac.uns.eventplanner.team7.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.fragments.CreateEventTypeFragment;
import rs.ac.uns.eventplanner.team7.fragments.EventTypeCategoryManipulationFragment;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    private final Context context;
    private List<CategoryResponseDTO> selectedCategories;
    private List<CategoryResponseDTO> addableCategories;

    public CategorySelectAdapter(Context context, List<CategoryResponseDTO> categories, List<CategoryResponseDTO> addableCategories) {
        this.context = context;
        this.selectedCategories = categories;
        this.addableCategories = addableCategories;
    }

    @NonNull
    @Override
    public CategorySelectAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.horizontal_button_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategorySelectAdapter.ViewHolder holder, int position) {
        CategoryResponseDTO category = selectedCategories.get(position);
        holder.titleView.setText(category.getName());
        holder.subtitleView.setVisibility(View.GONE);

        holder.fabView.setImageResource(R.drawable.baseline_cancel_24);

        holder.fabView.setOnClickListener(v -> {
            selectedCategories.remove(category);
            addableCategories.add(category);
            notifyDataSetChanged();

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;

                Fragment parentFragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.frameLayout);

                if (parentFragment instanceof CreateEventTypeFragment) {
                    EventTypeCategoryManipulationFragment fragment = (EventTypeCategoryManipulationFragment)
                            ((CreateEventTypeFragment) parentFragment).getChildFragmentManager()
                                    .findFragmentByTag("EventTypeCategoryManipulationFragmentTag");

                    if (fragment != null) {
                        fragment.notifyChange();
                    }
                }
            }
        });
    }


    @Override
    public int getItemCount() {
        return selectedCategories.size();
    }

    public void updateData(List<CategoryResponseDTO> newCategories) {
        selectedCategories.clear();
        selectedCategories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        FloatingActionButton fabView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.horizontal_card_title);
            subtitleView = itemView.findViewById(R.id.horizontal_card_subtitle);
            fabView = itemView.findViewById(R.id.horizontal_card_fab);
        }
    }
}
