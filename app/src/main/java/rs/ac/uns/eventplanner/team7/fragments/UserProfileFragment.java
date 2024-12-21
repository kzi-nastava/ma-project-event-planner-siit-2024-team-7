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
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.SimpleCarouselAdapter;
import rs.ac.uns.eventplanner.team7.dto.user.GetOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.DrawableComparator;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class UserProfileFragment extends Fragment {

    private UserRole role;
    private UserService userService;

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
        userService = ClientUtils.retrofit.create(UserService.class);
        setupRole(view);
        setupInputs(view);
        setupAllCarousels(view);
        fillFields(view);
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

    private void fillFields(View view) {
         Integer userId = JwtUtil.extractId(this.requireContext());
         String fullValue = "Bearer ";
         fullValue += JwtUtil.getToken(this.requireContext());
         if (role == UserRole.EVENT_ORG) {
             Call<GetOrganizerResponseDTO> call = userService.getOrganizer(fullValue, userId);
             call.enqueue(new Callback<GetOrganizerResponseDTO>() {
                 @Override
                 public void onResponse(Call<GetOrganizerResponseDTO> call, Response<GetOrganizerResponseDTO> response) {
                     GetOrganizerResponseDTO dto = response.body();
                     fillFields(view, dto, null);
                 }
                 @Override
                 public void onFailure(Call<GetOrganizerResponseDTO> call, Throwable t) {
                     Log.d("fail", Objects.requireNonNull(t.getMessage()));
                 }
             });
         }
         else if (role == UserRole.SPP) {
             Call<GetProviderResponseDTO> call = userService.getProvider(fullValue, userId);
             call.enqueue(new Callback<GetProviderResponseDTO>() {
                 @Override
                 public void onResponse(Call<GetProviderResponseDTO> call, Response<GetProviderResponseDTO> response) {
                     GetProviderResponseDTO dto = response.body();
                     fillFields(view, null, dto);
                 }

                 @Override
                 public void onFailure(Call<GetProviderResponseDTO> call, Throwable t) {
                 }
             });
         }
    }

    private void fillFields(View view, GetOrganizerResponseDTO orgDto, GetProviderResponseDTO proDto) {
        String email;
        String phone;
        String country;
        String city;
        String street;
        String houseNumber;
        String photo;
        if (orgDto == null) { // fill with proDto data
            email = proDto.getEmail();
            phone = proDto.getPhone();
            country = proDto.getLocation().getCountry();
            city = proDto.getLocation().getCity();
            street = proDto.getLocation().getStreet();
            houseNumber = proDto.getLocation().getHouseNumber();
            photo = proDto.getPhotoURL();
            fillField(view, R.id.change_org_name, proDto.getOrgName());
            fillField(view, R.id.change_org_desc, proDto.getOrgDesc());
        }
        else {
            email = orgDto.getEmail();
            phone = orgDto.getPhone();
            country = orgDto.getLocation().getCountry();
            city = orgDto.getLocation().getCity();
            street = orgDto.getLocation().getStreet();
            houseNumber = orgDto.getLocation().getHouseNumber();
            photo = orgDto.getPhotoURL();
            fillField(view, R.id.change_first_name, orgDto.getFirstName());
            fillField(view, R.id.change_last_name, orgDto.getLastName());
        }
        fillField(view, R.id.email_user_profile, email);
        fillField(view, R.id.change_phone, phone);
        fillField(view, R.id.change_country, country);
        fillField(view, R.id.change_city, city);
        fillField(view, R.id.change_street, street);
        fillField(view, R.id.change_house_number, houseNumber);
        fillField(view, R.id.change_profile_pic, photo);
        ShapeableImageView profile_pic = view.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(photo)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(profile_pic);
    }

    private void fillField(View view, int fieldId, String data) {
        TextInputEditText layout = view.findViewById(fieldId);
        layout.setText(data);
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