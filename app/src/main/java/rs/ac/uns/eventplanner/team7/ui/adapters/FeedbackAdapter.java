package rs.ac.uns.eventplanner.team7.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;

public class FeedbackAdapter extends RecyclerView.Adapter<FeedbackAdapter.ViewHolder> {
    public interface OnDecideClickListener {
        void onDecideClicked(FeedbackDTO feedback);
    }

    private final Object mutex = new Object();
    private final List<FeedbackDTO> feedback;
    private final OnDecideClickListener onDecideClickListener;
    private RecyclerView recyclerView;
    private int expandedPosition = -1;

    public FeedbackAdapter(List<FeedbackDTO> feedback, OnDecideClickListener onDecideClickListener) {
        this.feedback = feedback;
        this.onDecideClickListener = onDecideClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.expandable_card, parent, false);
        return new FeedbackAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.bind(feedback.get(position));
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public int getItemCount() {
        return feedback.size();
    }

    public int getLastItemIndex() {
        return getItemCount()-1;
    }

    public void addAll(@NonNull Collection<FeedbackDTO> feedback) {
        int initialSize;
        synchronized (mutex) {
            initialSize = this.feedback.size();
            this.feedback.addAll(feedback);
        }
        notifyItemRangeInserted(initialSize, feedback.size());
    }

    public void clear() {
        if (getItemCount() == 0) return;
        int size;
        synchronized (mutex) {
            size = feedback.size();
            feedback.clear();
        }
        notifyItemRangeRemoved(0, size);
    }    

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout header, body;
        private final ImageView leadingIcon, expandIcon;
        private final MaterialTextView title, topRightText, bodyText, bottomText, optionalBottomText;
        private final MaterialButton decideButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
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
        
        public void bind(FeedbackDTO feedback) {
            var context = itemView.getContext();
            formatButton(context);

            formatHeader(feedback, context);

            formatTopRightText(feedback);

            formatBody(feedback.getComment());

            formatBottomText(feedback, context);

            header.setOnClickListener(v -> onHeaderClickListener());

            decideButton.setOnClickListener(v -> onDecideClickListener.onDecideClicked(feedback));
        }

        private void formatButton(Context context) {
            decideButton.setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_flowchart));
            decideButton.setText(R.string.decide);
        }

        private void formatHeader(FeedbackDTO feedback, Context context) {
            String titleText;
            int drawableRes;
            if (feedback.getEventName() != null) {
                titleText = context.getString(R.string.event_feedback, feedback.getEventName());
                drawableRes = R.drawable.ic_event;
            } else if (feedback.getItemName() != null) {
                titleText = context.getString(R.string.item_feedback, feedback.getItemName());
                drawableRes = R.drawable.ic_hand_package;
            } else if (feedback.getProviderOrganization() != null) {
                titleText = context.getString(R.string.organizer_feedback, feedback.getProviderOrganization());
                drawableRes = R.drawable.ic_corporate_fare;
            } else throw new IllegalStateException("Feedback must have either event, item or organizer!");
            title.setText(titleText);
            leadingIcon.setImageDrawable(AppCompatResources.getDrawable(context, drawableRes));
        }

        private void formatTopRightText(FeedbackDTO feedback) {
            var currentYear = LocalDateTime.now().getYear();
            var feedbackDate = feedback.getCreatedAt();
            String pattern = String.format("EEEE, MMMM dd %s HH:mm", feedbackDate.getYear() == currentYear ? "" : "yyyy");
            topRightText.setText(
                    feedbackDate.format(DateTimeFormatter.ofPattern(pattern).withLocale(Locale.US))
            );
        }

        private void formatBody(String comment) {
            bodyText.setText(comment);
            body.setVisibility(View.GONE);
            expandIcon.setRotation(0f);
        }

        private void formatBottomText(FeedbackDTO feedback, Context context) {
            bottomText.setText(context.getString(R.string.rated, feedback.getRating()));
            optionalBottomText.setText(context.getString(R.string.submitted_by_user, feedback.getUserEmail()));
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
