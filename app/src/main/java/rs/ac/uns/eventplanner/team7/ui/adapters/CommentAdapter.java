package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    public interface OnReportCommentClickListener {
        void onReportCommentClicked(FeedbackDTO feedback);
    }

    private final Object mutex = new Object();
    private final Context context;
    private final List<FeedbackDTO> feedbackDTOs;
    private final String email; // will be null for Guest user

    @Setter
    private OnReportCommentClickListener onReportCommentClickListener;

    public CommentAdapter(Context context, List<FeedbackDTO> feedbackDTOs) {
        this.context = context;
        this.feedbackDTOs = feedbackDTOs;
        email = AuthUtil.extractEmail(context);
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

    public class ViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView title, comment, date;
        MaterialButton reportCommentButton;

        public ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.comment_author);
            comment = itemView.findViewById(R.id.comment_message);
            date = itemView.findViewById(R.id.comment_date);
            reportCommentButton = itemView.findViewById(R.id.report_comment_button);
        }

        public void bindData(FeedbackDTO feedbackDTO) {
            title.setText(feedbackDTO.getUserEmail());
            comment.setText(feedbackDTO.getComment());
            formatDate(feedbackDTO.getCreatedAt());
            boolean showReportButton = email != null && !email.equals(feedbackDTO.getUserEmail());
            reportCommentButton.setVisibility(showReportButton ? View.VISIBLE : View.GONE);
            reportCommentButton.setOnClickListener(v ->
                    onReportCommentClickListener.onReportCommentClicked(
                            feedbackDTO
                    )
            );
        }

        private void formatDate(LocalDateTime createdAt) {
            var createdDate = createdAt.toLocalDate();
            var todayDate = LocalDate.now();
            var daysDiff = ChronoUnit.DAYS.between(createdDate, todayDate);
            String dayLabel;
            if (daysDiff == 0) {
                dayLabel = context.getString(R.string.today);
            } else if (daysDiff == 1) {
                dayLabel = context.getString(R.string.yesterday);
            } else {
                var sameYear = todayDate.getYear() == createdDate.getYear();
                var pattern = sameYear ? "MMM d" : "MMM d, yyyy";
                dayLabel = DateTimeFormatter.ofPattern(pattern, Locale.getDefault()).format(createdDate);
            }

            var timeLabel = DateTimeFormatter.ofPattern("HH:mm").format(createdAt.toLocalTime());

            date.setText(String.format("%s at %s", dayLabel, timeLabel));
        }
    }
}
