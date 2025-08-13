package rs.ac.uns.eventplanner.team7.ui.fragments.reporting;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import java.util.Locale;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;


public class ReportReasonDialogFragment extends DialogFragment implements TextWatcher {

    public interface OnSubmitClickListener {
        void onSubmitClicked(String reportReason);
    }

    private TextInputEditText reasonInput;
    private MaterialTextView characterCount, inputError;

    @Setter
    private OnSubmitClickListener onSubmitClickListener;

    public ReportReasonDialogFragment() {
        // Required empty public constructor
    }


    public static ReportReasonDialogFragment newInstance() {
        return new ReportReasonDialogFragment();
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_MaterialDialogStyle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_report_reason_dialog, container, false);
        reasonInput = view.findViewById(R.id.report_reason_input);
        characterCount = view.findViewById(R.id.character_count);
        inputError = view.findViewById(R.id.input_error);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MaterialButton submitButton = view.findViewById(R.id.submit_btn);
        MaterialButton cancelButton = view.findViewById(R.id.cancel_btn);

        cancelButton.setOnClickListener(v -> dismiss());

        submitButton.setOnClickListener(v -> handleSubmit());

        reasonInput.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable text) {
        String reason = text != null? text.toString().trim() : "";
        characterCount.setText(String.format(Locale.getDefault(), "%d", reason.length()));
        checkConstraints(reason);
    }

    private void handleSubmit() {
        var text = reasonInput.getText();
        String reason = text != null? text.toString().trim() : "";
        if (checkConstraints(reason)) {
            onSubmitClickListener.onSubmitClicked(reason);
            dismiss();
        }
    }

    private boolean checkConstraints(String reason) {
        boolean isEmpty = reason.isEmpty();
        if (isEmpty) {
            inputError.setText(R.string.report_reason_required);
            return false;
        }
        if (reason.length() < 20) {
            inputError.setText(R.string.report_reason_min_length);
            return false;
        } else if (reason.length() > 500) {
            inputError.setText(R.string.report_reason_max_length);
            return false;
        }
        inputError.setText("");
        return true;
    }
}