package rs.ac.uns.eventplanner.team7.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.SimpleCarouselAdapter;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.utils.DrawableComparator;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class UserProfileFragment extends Fragment {

    private UserRole role;

    public UserProfileFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        setupRole(view);
        setupInputs(view);
        setupAllCarousels(view);
        setupFields(view);
        MaterialButton changePass = view.findViewById(R.id.change_password);
        changePass.setOnClickListener(v -> showChangePasswordDialog(this.requireContext()));
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        // Initialize 'role' here, as the fragment is now attached to a context
        String roleString = JwtUtil.getRole(context);
        this.role = UserRole.valueOf(roleString);
    }

    private void setupRole(View view) {
        MaterialTextView role = view.findViewById(R.id.user_role);
        if (this.role == UserRole.EVENT_ORG) {
            role.setText(R.string.you_eo);
            view.findViewById(R.id.eo_update_inputs).setVisibility(View.VISIBLE);
        }
        else if (this.role == UserRole.SPP) {
            role.setText(R.string.you_spp);
            view.findViewById(R.id.spp_update_inputs).setVisibility(View.VISIBLE);
        }
        else if (this.role == UserRole.AUTH) {
            role.setText(R.string.you_au);
        }
        else if (this.role == UserRole.ADMIN) {
            role.setText(R.string.you_admin);
        }
    }

    private void setupInputs(View view) {
        int editIconRes = R.drawable.ic_edit;
        int checkIconRes = R.drawable.ic_check;

        List<Pair<TextInputLayout, TextInputEditText>> fields = new ArrayList<>(); // Use ArrayList for a mutable list

        fields.add(new Pair<>(view.findViewById(R.id.change_phone_layout), view.findViewById(R.id.change_phone)));
        fields.add(new Pair<>(view.findViewById(R.id.change_country_layout), view.findViewById(R.id.change_country)));
        fields.add(new Pair<>(view.findViewById(R.id.change_city_layout), view.findViewById(R.id.change_city)));
        fields.add(new Pair<>(view.findViewById(R.id.change_street_layout), view.findViewById(R.id.change_street)));
        fields.add(new Pair<>(view.findViewById(R.id.change_house_number_layout), view.findViewById(R.id.change_house_number)));

        if (role == UserRole.EVENT_ORG) {
            fields.add(new Pair<>(view.findViewById(R.id.change_first_name_layout), view.findViewById(R.id.change_first_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_last_name_layout), view.findViewById(R.id.change_last_name)));
        }
        else if (role == UserRole.SPP) {
            fields.add(new Pair<>(view.findViewById(R.id.change_org_name_layout), view.findViewById(R.id.change_org_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_org_desc_layout), view.findViewById(R.id.change_org_desc)));
        }

        for (Pair<TextInputLayout, TextInputEditText> field : fields) {
            TextInputLayout layout = field.first;
            TextInputEditText input = field.second;

            layout.setTag(editIconRes); // Initialize with edit icon
            layout.setEndIconOnClickListener(v -> {
                if ((int) layout.getTag() == editIconRes) {
                    layout.setEndIconDrawable(checkIconRes);
                    input.setEnabled(true);
                    layout.setTag(checkIconRes);
                } else if ((int) layout.getTag() == checkIconRes) {
                    layout.setEndIconDrawable(editIconRes);
                    input.setEnabled(false);
                    layout.setTag(editIconRes);
                }
            });
        }
    }

    private void setupAllCarousels(View view) {
        setupCarousel(view, R.id.favouriteEventsCarousel);
        setupCarousel(view, R.id.favouriteServicesCarousel);
        setupCarousel(view, R.id.favouriteProductsCarousel);
    }

    private void setupCarousel(View view, int carouselId) {
        RecyclerView favoritesCarousel = view.findViewById(carouselId);

        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        favoritesCarousel.setLayoutManager(layoutManager);

        SimpleCarouselAdapter adapter = new SimpleCarouselAdapter(R.drawable.image_placeholder, 4);
        favoritesCarousel.setAdapter(adapter);
    }

    private void setupFields(View view) {

    }

    public void showChangePasswordDialog(Context context) {
        // Inflate the custom layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_change_password, null);

        TextInputEditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);

        // Build and show the dialog
        new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton("Change password", (dialog, which) -> {
                    // Get user inputs
                    String oldPassword = etOldPassword.getText().toString().trim();
                    String newPassword = etNewPassword.getText().toString().trim();
                    String confirmPassword = etConfirmPassword.getText().toString().trim();

                    // Validation logic
                    if (newPassword.isEmpty() || confirmPassword.isEmpty() || oldPassword.isEmpty()) {
                        Toast.makeText(context, "All fields are required.", Toast.LENGTH_SHORT).show();
                    } else if (!newPassword.equals(confirmPassword)) {
                        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show();
                    } else {
                        // Proceed with password change logic
                        Toast.makeText(context, "Password changed successfully.", Toast.LENGTH_SHORT).show();
                    }
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .create()
                .show();
    }

}