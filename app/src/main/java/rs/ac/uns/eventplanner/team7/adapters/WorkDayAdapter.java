package rs.ac.uns.eventplanner.team7.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import rs.ac.uns.eventplanner.team7.model.WorkDay;

public class WorkDayAdapter extends RecyclerView.Adapter<WorkDayAdapter.ViewHolder> {
    private final List<WorkDay> workDaysList;

    public WorkDayAdapter(Set<WorkDay> workDaysSet) {
        this.workDaysList = new ArrayList<>(workDaysSet);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WorkDay workDay = workDaysList.get(position);
        holder.dayOfWeek.setText(workDay.getDay().toString());
        holder.workTime.setText(String.format("%s - %s", workDay.getWorkTimeStart(), workDay.getWorkTimeEnd()));
    }

    @Override
    public int getItemCount() {
        return workDaysList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView dayOfWeek;
        TextView workTime;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            dayOfWeek = itemView.findViewById(android.R.id.text1);
            workTime = itemView.findViewById(android.R.id.text2);
        }
    }
}

