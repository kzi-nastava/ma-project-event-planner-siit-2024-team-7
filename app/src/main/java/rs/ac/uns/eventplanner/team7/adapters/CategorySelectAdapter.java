package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.fragments.admin.event_types.CreateEventTypeFragment;
import rs.ac.uns.eventplanner.team7.fragments.admin.event_types.EventTypeCategoryManipulationFragment;
import rs.ac.uns.eventplanner.team7.fragments.admin.event_types.UpdateEventTypeFragment;
import rs.ac.uns.eventplanner.team7.model.Category;

public class CategorySelectAdapter extends RecyclerView.Adapter<CategorySelectAdapter.ViewHolder> {

    private final Context context;
    private final List<Category> selectedCategories;
    private final List<Category> addableCategories;

    public CategorySelectAdapter(Context context, List<Category> categories, List<Category> addableCategories) {
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
        Category category = selectedCategories.get(position);
        holder.subtitleView.setText(category.getName());
        holder.titleView.setVisibility(View.INVISIBLE);
        holder.button.setOnClickListener(v -> {
            selectedCategories.remove(category);
            addableCategories.add(category);
            notifyDataSetChanged();

            if (context instanceof FragmentActivity) {
                FragmentActivity activity = (FragmentActivity) context;

                Fragment parentFragment = activity.getSupportFragmentManager()
                        .findFragmentById(R.id.nav_host_fragment);

                if (parentFragment instanceof CreateEventTypeFragment) {
                    EventTypeCategoryManipulationFragment fragment = (EventTypeCategoryManipulationFragment)
                            parentFragment.getChildFragmentManager()
                                    .findFragmentByTag("EventTypeCategoryManipulationFragmentTag");

                    if (fragment != null) {
                        fragment.notifyChange();
                    }
                }

                if (parentFragment instanceof UpdateEventTypeFragment) {
                    EventTypeCategoryManipulationFragment fragment = (EventTypeCategoryManipulationFragment)
                            parentFragment.getChildFragmentManager()
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

    public void updateData(List<Category> newCategories) {
        selectedCategories.clear();
        selectedCategories.addAll(newCategories);
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        MaterialButton button;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            button = itemView.findViewById(R.id.card_more_info_button);
        }
    }
}
