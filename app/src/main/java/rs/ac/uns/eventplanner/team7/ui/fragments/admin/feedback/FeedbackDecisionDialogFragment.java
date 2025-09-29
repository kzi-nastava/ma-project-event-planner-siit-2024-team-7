package rs.ac.uns.eventplanner.team7.ui.fragments.admin.feedback;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.enums.FeedbackStatus;


public class FeedbackDecisionDialogFragment extends BottomSheetDialogFragment {
    public interface OnDecisionClickListener {
        void onDecisionClicked(FeedbackStatus decidedStatus);
    }

    @Setter
    private OnDecisionClickListener onDecisionClickListener;

    private FeedbackStatus decidedStatus;

    public FeedbackDecisionDialogFragment() {
        // Required empty public constructor
    }

    public static FeedbackDecisionDialogFragment newInstance() {
        return new FeedbackDecisionDialogFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_feedback_decision_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MaterialButton submitDecisionButton = view.findViewById(R.id.submit_decision_button);

        MaterialButton closeButton = view.findViewById(R.id.feedback_decision_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        RadioGroup decisionGroup = view.findViewById(R.id.feedback_decision_radio_group);
        decisionGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.accept_radio_button) {
                decidedStatus = FeedbackStatus.APPROVED;
            } else if (checkedId == R.id.reject_radio_button) {
                decidedStatus = FeedbackStatus.REMOVED;
            }
            submitDecisionButton.setEnabled(true);
        });

        submitDecisionButton.setOnClickListener(v -> {
            onDecisionClickListener.onDecisionClicked(decidedStatus);
            dismiss();
        });
    }
}