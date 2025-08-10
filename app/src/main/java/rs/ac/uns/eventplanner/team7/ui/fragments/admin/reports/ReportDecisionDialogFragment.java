package rs.ac.uns.eventplanner.team7.ui.fragments.admin.reports;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.radiobutton.MaterialRadioButton;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.model.enums.ReportDecision;


public class ReportDecisionDialogFragment extends BottomSheetDialogFragment {

    public interface OnDecideClickListener {
        void onDecisionClicked(ReportDecision decision);
    }

    private static final String ARG_IS_FEEDBACK_REPORT = "is_feedback_report";

    @Setter
    private OnDecideClickListener onDecideClickListener;

    private ReportDecision decision;

    boolean isFeedbackReport;

    public ReportDecisionDialogFragment() {
        // Required empty public constructor
    }

    public static ReportDecisionDialogFragment newInstance(boolean isFeedbackReport) {
        ReportDecisionDialogFragment fragment = new ReportDecisionDialogFragment();
        Bundle args = new Bundle();
        args.putBoolean(ARG_IS_FEEDBACK_REPORT, isFeedbackReport);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            isFeedbackReport = getArguments().getBoolean(ARG_IS_FEEDBACK_REPORT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_decision_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton submitDecisionButton = view.findViewById(R.id.submit_decision_button);

        MaterialButton closeButton = view.findViewById(R.id.report_decision_close_button);
        closeButton.setOnClickListener(v -> dismiss());

        MaterialRadioButton deleteFeedback = view.findViewById(R.id.delete_feedback_radio_button);
        MaterialRadioButton deleteFeedbackSuspend = view.findViewById(R.id.delete_feedback_and_suspend_radio_button);
        if (!isFeedbackReport) {
            deleteFeedback.setVisibility(View.GONE);
            deleteFeedbackSuspend.setVisibility(View.GONE);
        }
        RadioGroup reportDecisions = view.findViewById(R.id.report_decision_radio_group);
        reportDecisions.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.ignore_radio_button) {
                decision = ReportDecision.IGNORE;
            } else if (checkedId == R.id.suspend_radio_button) {
                decision = ReportDecision.SUSPEND;
            } else if (checkedId == R.id.delete_feedback_radio_button) {
                decision = ReportDecision.DELETE_FEEDBACK;
            } else if (checkedId == R.id.delete_feedback_and_suspend_radio_button) {
                decision = ReportDecision.DELETE_FEEDBACK_AND_SUSPEND;
            }
            submitDecisionButton.setEnabled(true);
        });

        submitDecisionButton.setOnClickListener(v -> {
            onDecideClickListener.onDecisionClicked(decision);
            dismiss();
        });
    }
}