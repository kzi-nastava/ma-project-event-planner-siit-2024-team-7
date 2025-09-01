package rs.ac.uns.eventplanner.team7.ui.fragments.user;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import lombok.Setter;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.data.dto.user.UpgradeAuthUserRequestDTO;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;


public class UpgradeAccountDialogFragment extends DialogFragment {

    public interface OnConfirmClickListener {
        void onConfirmClicked(UpgradeAuthUserRequestDTO requestDTO);
    }

    @Setter
    private OnConfirmClickListener onConfirmClickListener;

    private TextInputLayout nameInputLayout, lastNameInputLayout, orgDescInputLayout, passwordInputLayout;
    private TextInputEditText nameInput, lastNameInput, orgDescInput, confirmPasswordInput;
    private MaterialButton confirmButton, cancelButton;

    private UserRole selectedRole;

    public UpgradeAccountDialogFragment() {
        // Required empty public constructor
    }

    public static UpgradeAccountDialogFragment newInstance() {
        return new UpgradeAccountDialogFragment();
    }

    @Override
    public int getTheme() {
        return R.style.AppTheme_MaterialDialogStyle;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upgrade_account_dialog, container, false);

        nameInputLayout = view.findViewById(R.id.nameInputLayout);
        lastNameInputLayout = view.findViewById(R.id.lastNameInputLayout);
        orgDescInputLayout = view.findViewById(R.id.orgDescInputLayout);
        passwordInputLayout = view.findViewById(R.id.passwordInputLayout);

        nameInput = view.findViewById(R.id.name_input);
        lastNameInput = view.findViewById(R.id.last_name_input);
        orgDescInput = view.findViewById(R.id.org_desc_input);
        confirmPasswordInput = view.findViewById(R.id.confirm_password_input);

        confirmButton = view.findViewById(R.id.confirm_button);
        cancelButton = view.findViewById(R.id.cancel_button);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupValidation();

        NestedScrollView scrollView = view.findViewById(R.id.dialog_content);

        RadioGroup roleGroup = view.findViewById(R.id.role_group);
        roleGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (selectedRole == null) {
                passwordInputLayout.setVisibility(View.VISIBLE);
                nameInputLayout.setVisibility(View.VISIBLE);
            }
            if (checkedId == R.id.event_org_rb) {
                selectedRole = UserRole.EVENT_ORG;
                lastNameInputLayout.setVisibility(View.VISIBLE);
                orgDescInputLayout.setVisibility(View.GONE);
                nameInputLayout.setHint(getString(R.string.first_name));
            } else if (checkedId == R.id.spp_rb) {
                selectedRole = UserRole.SPP;
                lastNameInputLayout.setVisibility(View.GONE);
                orgDescInputLayout.setVisibility(View.VISIBLE);
                nameInputLayout.setHint(getString(R.string.org_name));
            }
            scrollView.post(() -> scrollView.smoothScrollTo(0, scrollView.getBottom()));
        });

        cancelButton.setOnClickListener(v -> dismiss());

        confirmButton.setOnClickListener(v -> {
            boolean valid = validateName() & validateLastName() & validateOrgDesc() & validatePassword();
            if (selectedRole != null && valid && onConfirmClickListener != null) {
                onConfirmClickListener.onConfirmClicked(getDTO());
                dismiss();
            }
        });

    }

    @NonNull
    private UpgradeAuthUserRequestDTO getDTO() {
        String name = nameInput.getText() != null ? nameInput.getText().toString().trim() : null;
        return new UpgradeAuthUserRequestDTO(
                name,
                lastNameInput.getText() != null ? lastNameInput.getText().toString().trim() : null,
                name, // orgName reuses name
                orgDescInput.getText() != null ? orgDescInput.getText().toString().trim() : null,
                selectedRole,
                confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString().trim() : null
        );
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    (int) (getResources().getDisplayMetrics().heightPixels * 0.75) // 75% of screen height
            );
        }
    }

    private void setupValidation() {
        nameInput.addTextChangedListener((SimpleTextWatcher) s -> validateName());
        lastNameInput.addTextChangedListener((SimpleTextWatcher) s -> validateLastName());
        orgDescInput.addTextChangedListener((SimpleTextWatcher) s -> validateOrgDesc());
        confirmPasswordInput.addTextChangedListener((SimpleTextWatcher) s -> validatePassword());
    }

    private boolean validateName() {
        if (TextUtils.isEmpty(nameInput.getText())) {
            int stringRes = selectedRole == UserRole.EVENT_ORG
                    ? R.string.first_name_is_required
                    : R.string.organization_name_is_required;
            nameInputLayout.setError(getString(stringRes));
            return false;
        }
        nameInputLayout.setError(null);
        return true;
    }

    private boolean validateLastName() {
        if (selectedRole == UserRole.EVENT_ORG && TextUtils.isEmpty(lastNameInput.getText())) {
            lastNameInputLayout.setError(getString(R.string.last_name_is_required));
            return false;
        }
        lastNameInputLayout.setError(null);
        return true;
    }

    private boolean validateOrgDesc() {
        if (selectedRole == UserRole.SPP && TextUtils.isEmpty(orgDescInput.getText())) {
            orgDescInputLayout.setError(getString(R.string.organization_description_is_required));
            return false;
        }
        orgDescInputLayout.setError(null);
        return true;
    }

    private boolean validatePassword() {
        String password = confirmPasswordInput.getText() != null ? confirmPasswordInput.getText().toString() : "";
        if (password.isEmpty()) {
            passwordInputLayout.setError(getString(R.string.password_is_required));
            return false;
        } else if (password.length() < 8) {
            passwordInputLayout.setError(getString(R.string.password_min_length_error));
            return false;
        }
        passwordInputLayout.setError(null);
        return true;
    }

    private interface SimpleTextWatcher extends TextWatcher {
        default void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        default void onTextChanged(CharSequence s, int start, int before, int count) {}
        void afterTextChanged(Editable s);
    }

}