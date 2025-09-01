package rs.ac.uns.eventplanner.team7.ui.fragments;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class MaterialDialogFragment extends DialogFragment {
    private View dialogView;

    public MaterialDialogFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireContext(), getTheme());
        dialogView = onCreateView(getLayoutInflater(), null, savedInstanceState);

        if (dialogView != null) {
            onViewCreated(dialogView, savedInstanceState);
        }
        builder.setView(dialogView);
        return builder.create();
    }

    @Nullable
    @Override
    public View getView() {
        return dialogView;
    }
}