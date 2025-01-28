package rs.ac.uns.eventplanner.team7.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class ChangePasswordFragment extends MaterialDialogFragment {

    private static final String ARG_ROLE = "user_role";

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private UserRole role;

    // Factory method to create an instance with the role argument
    public static ChangePasswordFragment newInstance(UserRole role) {
        ChangePasswordFragment fragment = new ChangePasswordFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ROLE, role);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            role = (UserRole) getArguments().getSerializable(ARG_ROLE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext());
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_change_password, null);

        TextInputEditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
        MaterialTextView errorMsg = dialogView.findViewById(R.id.change_pass_error);

        builder.setView(dialogView)
                .setPositiveButton(R.string.change_pass, null) // Set to null for custom behavior
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                String oldPassword = Objects.requireNonNull(etOldPassword.getText()).toString().trim();
                String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();
                String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

                if (validateInputs(oldPassword, newPassword, confirmPassword, errorMsg)) {
                    if (role == UserRole.EVENT_ORG) {
                        UpdateOrganizerRequestDTO dto = createOrganizerDTO(oldPassword, newPassword, confirmPassword);
                        userService.updateOrganizer(
                                JwtUtil.getAuthorizationValue(requireContext()),
                                JwtUtil.extractId(requireContext()),
                                dto
                        ).enqueue((Callback<UpdateOrganizerResponseDTO>) createPasswordChangeCallback(errorMsg));
                    } else if (role == UserRole.SPP) {
                        UpdateProviderRequestDTO dto = createProviderDTO(oldPassword, newPassword, confirmPassword);
                        userService.updateProvider(
                                JwtUtil.getAuthorizationValue(requireContext()),
                                JwtUtil.extractId(requireContext()),
                                dto
                        ).enqueue((Callback<UpdateProviderResponseDTO>) createPasswordChangeCallback(errorMsg));
                    }
                }
            });
        });

        return dialog;
    }

    private boolean validateInputs(String oldPassword, String newPassword, String confirmPassword, MaterialTextView errorMsg) {
        if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
            errorMsg.setText(R.string.all_fields_are_required);
            errorMsg.setVisibility(View.VISIBLE);
            return false;
        }
        if (!newPassword.equals(confirmPassword)) {
            errorMsg.setText(R.string.new_passwords_do_not_match);
            errorMsg.setVisibility(View.VISIBLE);
            return false;
        }
        return true;
    }

    private UpdateOrganizerRequestDTO createOrganizerDTO(String oldPassword, String newPassword, String confirmPassword) {
        UpdateOrganizerRequestDTO dto = new UpdateOrganizerRequestDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword1(newPassword);
        dto.setNewPassword2(confirmPassword);
        return dto;
    }

    private UpdateProviderRequestDTO createProviderDTO(String oldPassword, String newPassword, String confirmPassword) {
        UpdateProviderRequestDTO dto = new UpdateProviderRequestDTO();
        dto.setOldPassword(oldPassword);
        dto.setNewPassword1(newPassword);
        dto.setNewPassword2(confirmPassword);
        return dto;
    }

    private Callback<?> createPasswordChangeCallback(MaterialTextView errorMsg) {
        return new Callback<Object>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (response.isSuccessful()) {
                    dismiss();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject jsonObject = new JSONObject(errorBody);
                        String message = jsonObject.getString("message");
                        errorMsg.setText(message);
                        errorMsg.setVisibility(View.VISIBLE);
                    } catch (Exception e) {
                        Log.e("ErrorParsing", "Failed to parse error response", e);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Object> call, @NonNull Throwable t) {
                errorMsg.setText(t.getMessage());
                errorMsg.setVisibility(View.VISIBLE);
            }
        };
    }
}
