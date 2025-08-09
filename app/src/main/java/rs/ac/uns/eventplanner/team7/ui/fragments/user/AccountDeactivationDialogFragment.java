package rs.ac.uns.eventplanner.team7.ui.fragments.user;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textview.MaterialTextView;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.ui.activities.LoginActivity;
import rs.ac.uns.eventplanner.team7.ui.fragments.MaterialDialogFragment;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.data.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class AccountDeactivationDialogFragment extends MaterialDialogFragment {

    private static final String ARG_ROLE = "user_role";

    private final UserService userService = ClientUtils.injectService(UserService.class);
    private UserRole role;

    // Factory method to create an instance with the role argument
    public static AccountDeactivationDialogFragment newInstance(UserRole role) {
        AccountDeactivationDialogFragment fragment = new AccountDeactivationDialogFragment();
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
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_account_deactivation, null);
        builder.setView(dialogView)
                .setPositiveButton(R.string.yes, null) // Set to null for custom behavior
                .setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());

        AlertDialog dialog = builder.create();

        dialog.setOnShowListener(d -> {
            MaterialTextView msg = dialogView.findViewById(R.id.deactivation_message);
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                if (role == UserRole.EVENT_ORG) {
                    userService.deactivateOrganizer(
                            AuthUtil.getAuthorizationValue(requireContext()),
                            AuthUtil.extractId(requireContext())
                    ).enqueue(createDeactivationCallback(msg));
                } else if (role == UserRole.SPP) {
                    userService.deactivateProvider(
                            AuthUtil.getAuthorizationValue(requireContext()),
                            AuthUtil.extractId(requireContext())
                    ).enqueue(createDeactivationCallback(msg));
                }
            });
        });

        return dialog;
    }

    private Callback<Object> createDeactivationCallback(MaterialTextView errorMsg) {
        return new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Object> call, @NonNull Response<Object> response) {
                if (!isAdded()) return;
                if (response.isSuccessful()) {
                    dismiss();
                    startActivity(new Intent(requireActivity(), LoginActivity.class));
                    requireActivity().finish();
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

