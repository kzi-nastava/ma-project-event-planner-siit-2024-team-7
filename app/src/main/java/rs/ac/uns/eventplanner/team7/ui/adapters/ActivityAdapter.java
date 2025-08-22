package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.Activity;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ActivityViewHolder> {

    private final List<Activity> activities;

    public ActivityAdapter(List<Activity> activities) {
        this.activities = activities;
    }

    @NonNull
    @Override
    public ActivityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_activity, parent, false);
        return new ActivityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityViewHolder holder, int position) {
        Activity activity = activities.get(position);
        holder.nameTv.setText(activity.getName());
        holder.timeTv.setText(activity.getStartTime() + " - " + activity.getEndTime());
        holder.locationTv.setText(activity.getLocation());
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    static class ActivityViewHolder extends RecyclerView.ViewHolder {
        TextView nameTv, timeTv, locationTv;

        public ActivityViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTv = itemView.findViewById(R.id.activity_name);
            timeTv = itemView.findViewById(R.id.activity_time);
            locationTv = itemView.findViewById(R.id.activity_location);
        }
    }
}

