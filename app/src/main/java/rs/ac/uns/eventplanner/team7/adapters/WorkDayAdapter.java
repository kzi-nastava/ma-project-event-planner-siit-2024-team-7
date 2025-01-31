package rs.ac.uns.eventplanner.team7.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.service.WorkDayDTO;

public class WorkDayAdapter extends RecyclerView.Adapter<WorkDayAdapter.ViewHolder> {
    private final Context context;
    private final List<WorkDayDTO> workDaysList;

    public WorkDayAdapter(Context context, List<WorkDayDTO> workDaysSet) {
        this.workDaysList = workDaysSet;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.horizontal_button_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkDayDTO workDay = workDaysList.get(position);
        holder.titleView.setText(workDay.getDay().toString());
        holder.subtitleView.setText(String.format("%s - %s", workDay.getWorkTimeStart(), workDay.getWorkTimeEnd()));
        holder.fabView.setImageResource(R.drawable.ic_cancel);  // Remove button

        holder.fabView.setOnClickListener(v -> {
            workDaysList.remove(workDay);
            notifyDataSetChanged();
            holder.setVisibility(View.GONE);
        });
        holder.itemView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return workDaysList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView titleView;
        MaterialTextView subtitleView;
        FloatingActionButton fabView;
        MaterialCardView cardView;

        public ViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.card_title);
            subtitleView = itemView.findViewById(R.id.card_subtitle);
            fabView = itemView.findViewById(R.id.card_more_info_button);
            cardView = itemView.findViewById(R.id.horizontal_button_card);
        }

        public void setVisibility(int visibility) {
            cardView.setVisibility(visibility);
        }
    }
}

