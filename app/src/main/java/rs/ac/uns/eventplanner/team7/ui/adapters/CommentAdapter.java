package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.Collection;
import java.util.List;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {

    private final Object mutex = new Object();
    private final Context context;
    private final List<FeedbackDTO> feedbackDTOs;

    public CommentAdapter(Context context, List<FeedbackDTO> feedbackDTOs) {
        this.context = context;
        this.feedbackDTOs = feedbackDTOs;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FeedbackDTO feedback = feedbackDTOs.get(position);
        holder.bindData(feedback);
    }

    @Override
    public int getItemCount() {
        return feedbackDTOs.size();
    }

    public void addAll(@NonNull Collection<FeedbackDTO> feedbacks) {
        int initialSize;
        synchronized (mutex) {
            initialSize = this.feedbackDTOs.size();
            this.feedbackDTOs.addAll(feedbacks);
        }
        notifyItemRangeInserted(initialSize, feedbacks.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = feedbackDTOs.size();
            feedbackDTOs.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView title, comment;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.comment_author);
            comment = itemView.findViewById(R.id.comment_message);
        }

        public void bindData(FeedbackDTO feedbackDTO) {
            title.setText(feedbackDTO.getUserEmail());
            comment.setText(feedbackDTO.getComment());
        }
    }
}
