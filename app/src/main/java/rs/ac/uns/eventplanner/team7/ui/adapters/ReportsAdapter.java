package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;

public class ReportsAdapter extends RecyclerView.Adapter<ReportsAdapter.ViewHolder> {
    public interface OnDecideClickListener {
        void onDecideClicked(ReportDTO report);
    }

    private final Object mutex = new Object();
    private final List<ReportDTO> reports;
    private final OnDecideClickListener onDecideClickListener;
    private RecyclerView recyclerView;
    private int expandedPosition = -1;

    public ReportsAdapter(List<ReportDTO> reports, OnDecideClickListener onDecideClickListener) {
        this.reports = reports;
        this.onDecideClickListener = onDecideClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expandable_card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(reports.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    public int getLastItemIndex() {
        return getItemCount()-1;
    }

    public void addAll(@NonNull Collection<ReportDTO> reports) {
        int initialSize;
        synchronized (mutex) {
            initialSize = this.reports.size();
            this.reports.addAll(reports);
        }
        notifyItemRangeInserted(initialSize, reports.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = reports.size();
            reports.clear();
        }
        notifyItemRangeRemoved(0, size);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private final MaterialCardView rootView;
        private final LinearLayout header, body;
        private final ImageView leadingIcon, expandIcon;
        private final MaterialTextView title, topRightText, bodyText, bottomText, optionalBottomText;
        private final MaterialButton decideButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rootView = itemView.findViewById(R.id.expandable_card);
            leadingIcon = itemView.findViewById(R.id.leading_icon);
            header = itemView.findViewById(R.id.card_header);
            body = itemView.findViewById(R.id.card_description);
            expandIcon = itemView.findViewById(R.id.expand_icon);
            title = itemView.findViewById(R.id.header_title);
            topRightText = itemView.findViewById(R.id.top_right_text);
            bodyText = itemView.findViewById(R.id.body_text);
            bottomText = itemView.findViewById(R.id.bottom_text);
            optionalBottomText = itemView.findViewById(R.id.optional_bottom_text);
            decideButton = itemView.findViewById(R.id.card_btn);
        }

        public void bind(ReportDTO report) {
            var context = itemView.getContext();
            boolean isDecided = report.isDecided();

            formatBackground(isDecided, context);

            formatButton(isDecided, context);

            formatHeader(report, context);

            formatTopRightText(report);

            formatBody(report.getReason());

            formatBottomText(report, context);

            header.setOnClickListener(v -> onHeaderClickListener());

            decideButton.setOnClickListener(v -> onDecideClickListener.onDecideClicked(report));
        }

        private void formatBackground(boolean isDecided, Context context) {
            if (!isDecided) {
                rootView.setStrokeColor(ContextCompat.getColor(context, R.color.red_delete));
            } else {
                rootView.setStrokeColor(ContextCompat.getColor(context, R.color.grey));
            }
        }

        private void formatButton(boolean isDecided, Context context) {
            decideButton.setVisibility(isDecided ? View.GONE : View.VISIBLE);
            decideButton.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_flowchart));
            decideButton.setText(R.string.decide);
        }

        private void formatHeader(ReportDTO report, Context context) {
            int titleRes = report.isDecided() ? R.string.resolved_for_user : R.string.reported_user;
            title.setText(context.getString(titleRes, report.getReportedUserEmail()));

            int drawableRes = report.isDecided() ? R.drawable.ic_assignment_turned_in : R.drawable.ic_assignment_late;
            leadingIcon.setImageDrawable(AppCompatResources.getDrawable(context, drawableRes));
        }

        private void formatBody(String reason) {
            bodyText.setText(reason);
            body.setVisibility(View.GONE);
            expandIcon.setRotation(0f);
        }

        private void formatTopRightText(ReportDTO report) {
            var currentYear = LocalDateTime.now().getYear();
            var parsedDate = LocalDateTime.parse(report.getReportDate());
            String pattern = String.format("EEEE, MMMM dd %s HH:mm", parsedDate.getYear() == currentYear ? "" : "yyyy");
            topRightText.setText(
                    parsedDate.format(DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US))
            );
        }

        private void formatBottomText(ReportDTO report, Context context) {
            optionalBottomText.setText(context.getString(R.string.submitted_by_user, report.getReporterEmail()));

            var feedback = report.getReportedFeedback();
            if (feedback != null) {
                String feedbackText = context.getString(R.string.reported_feedback) + '\n' +
                        feedback.getComment() + '\n' +
                        context.getString(R.string.reported_rating, feedback.getRating());
                bottomText.setText(feedbackText);
            }
            bottomText.setVisibility(feedback != null ? View.VISIBLE : View.GONE);
        }

        private void onHeaderClickListener() {
            int old = expandedPosition;
            if (isExpanded()) {
                // tapped the open one -> collapse it
                expandedPosition = -1;
                animateToggle(body, expandIcon, false);
                return;
            }
            // tapped a closed one -> expand it
            expandedPosition = getAdapterPosition();
            animateToggle(body, expandIcon, true);

            // also collapse previous holder (if still visible)
            if (old != -1) {
                RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(old);
                if (vh instanceof ViewHolder) {
                    ViewHolder oldVH = (ViewHolder)vh;
                    animateToggle(oldVH.body, oldVH.expandIcon, false);
                }
            }
        }

        private boolean isExpanded() {
            int position = getAdapterPosition();
            return position == expandedPosition;
        }

        private void animateToggle(View body, View arrow, boolean expand) {
            TransitionManager.beginDelayedTransition(recyclerView, new AutoTransition()
                    .setDuration(300)
                    .setOrdering(TransitionSet.ORDERING_TOGETHER));

            body.setVisibility(expand ? View.VISIBLE : View.GONE);

            arrow.animate().rotation(expand ? 180f : 0f).setDuration(200).start();
        }


    }
}