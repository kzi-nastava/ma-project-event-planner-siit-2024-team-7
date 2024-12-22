package rs.ac.uns.eventplanner.team7.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.adapters.SimpleCarouselAdapter;
import rs.ac.uns.eventplanner.team7.dto.user.GetOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.user.UpdateProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.UserService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class UserProfileFragment extends Fragment {

    private UserRole role;
    private UserService userService;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_profile, container, false);
        userService = ClientUtils.retrofit.create(UserService.class);

        setupRole(view);
        setupInputs(view);
        fillFields(view);
        setupAllCarousels(view);

        MaterialButton changePass = view.findViewById(R.id.change_password);
        changePass.setOnClickListener(v -> showChangePasswordDialog(requireContext()));

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        String roleString = JwtUtil.getRole(context);
        this.role = UserRole.valueOf(roleString);
    }

    private void setupRole(View view) {
        MaterialTextView roleTextView = view.findViewById(R.id.user_role);
        int visibility;

        switch (role) {
            case EVENT_ORG:
                roleTextView.setText(R.string.you_eo);
                visibility = View.VISIBLE;
                view.findViewById(R.id.eo_update_inputs).setVisibility(visibility);
                break;
            case SPP:
                roleTextView.setText(R.string.you_spp);
                visibility = View.VISIBLE;
                view.findViewById(R.id.spp_update_inputs).setVisibility(visibility);
                break;
            case AUTH:
                roleTextView.setText(R.string.you_au);
                break;
            case ADMIN:
                roleTextView.setText(R.string.you_admin);
                break;
        }
    }

    private void setupInputs(View view) {
        List<Pair<TextInputLayout, TextInputEditText>> fields = initializeFields(view);
        int editIconRes = R.drawable.ic_edit;
        int checkIconRes = R.drawable.ic_check;

        for (Pair<TextInputLayout, TextInputEditText> field : fields) {
            TextInputLayout layout = field.first;
            TextInputEditText input = field.second;

            layout.setTag(editIconRes);
            layout.setEndIconOnClickListener(v -> toggleFieldEditMode(layout, input, editIconRes, checkIconRes));
        }
    }

    private List<Pair<TextInputLayout, TextInputEditText>> initializeFields(View view) {
        List<Pair<TextInputLayout, TextInputEditText>> fields = new ArrayList<>();

        fields.add(new Pair<>(view.findViewById(R.id.change_phone_layout), view.findViewById(R.id.change_phone)));
        fields.add(new Pair<>(view.findViewById(R.id.change_country_layout), view.findViewById(R.id.change_country)));
        fields.add(new Pair<>(view.findViewById(R.id.change_city_layout), view.findViewById(R.id.change_city)));
        fields.add(new Pair<>(view.findViewById(R.id.change_street_layout), view.findViewById(R.id.change_street)));
        fields.add(new Pair<>(view.findViewById(R.id.change_house_number_layout), view.findViewById(R.id.change_house_number)));
        fields.add(new Pair<>(view.findViewById(R.id.change_profile_pic_layout), view.findViewById(R.id.change_profile_pic)));

        if (role == UserRole.EVENT_ORG) {
            fields.add(new Pair<>(view.findViewById(R.id.change_first_name_layout), view.findViewById(R.id.change_first_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_last_name_layout), view.findViewById(R.id.change_last_name)));
        } else if (role == UserRole.SPP) {
            fields.add(new Pair<>(view.findViewById(R.id.change_org_name_layout), view.findViewById(R.id.change_org_name)));
            fields.add(new Pair<>(view.findViewById(R.id.change_org_desc_layout), view.findViewById(R.id.change_org_desc)));
        }

        return fields;
    }

    private void toggleFieldEditMode(TextInputLayout layout, TextInputEditText input, int editIconRes, int checkIconRes) {
        if ((int) layout.getTag() == editIconRes) {
            layout.setEndIconDrawable(checkIconRes);
            input.setEnabled(true);
            layout.setTag(checkIconRes);
        } else {
            layout.setEndIconDrawable(editIconRes);
            input.setEnabled(false);
            layout.setTag(editIconRes);

            String fieldValue = Objects.requireNonNull(input.getText()).toString().trim();
            updateField(layout.getId(), fieldValue);
        }

    }

    private void updateField(int fieldId, String value) {
        Integer userId = JwtUtil.extractId(requireContext());
        String authHeader = JwtUtil.getAuthorizationValue(requireContext());

        if (role == UserRole.EVENT_ORG) {
            UpdateOrganizerRequestDTO dto = new UpdateOrganizerRequestDTO();
            if (fieldId == R.id.change_phone_layout) {
                dto.setPhone(value);
            } else if (fieldId == R.id.change_country_layout) {
                dto.getLocation().setCountry(value);
            } else if (fieldId == R.id.change_city_layout) {
                dto.getLocation().setCity(value);
            } else if (fieldId == R.id.change_street_layout) {
                dto.getLocation().setStreet(value);
            } else if (fieldId == R.id.change_house_number_layout) {
                dto.getLocation().setHouseNumber(value);
            } else if (fieldId == R.id.change_first_name_layout) {
                dto.setFirstName(value);
            } else if (fieldId == R.id.change_last_name_layout) {
                dto.setLastName(value);
            } else if (fieldId == R.id.change_profile_pic_layout) {
                dto.setPhotoURL(value);
            } else {
                return;
            }

            userService.updateOrganizer(authHeader, userId, dto).enqueue((Callback<UpdateOrganizerResponseDTO>) createUpdateFieldCallback());
        } else if (role == UserRole.SPP) {
            UpdateProviderRequestDTO dto = new UpdateProviderRequestDTO();
            if (fieldId == R.id.change_phone_layout) {
                dto.setPhone(value);
            } else if (fieldId == R.id.change_country_layout) {
                dto.getLocation().setCountry(value);
            } else if (fieldId == R.id.change_city_layout) {
                dto.getLocation().setCity(value);
            } else if (fieldId == R.id.change_street_layout) {
                dto.getLocation().setStreet(value);
            } else if (fieldId == R.id.change_house_number_layout) {
                dto.getLocation().setHouseNumber(value);
            }
            else if (fieldId == R.id.change_org_desc_layout) {
                dto.setOrgDesc(value);
            } else if (fieldId == R.id.change_profile_pic_layout) {
                dto.setPhotoURL(value);
            } else {
                return;
            }
            userService.updateProvider(authHeader, userId, dto).enqueue((Callback<UpdateProviderResponseDTO>) createUpdateFieldCallback());
        }
    }

    private Callback<?> createUpdateFieldCallback() {
        return new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    Log.d("UserProfileFragment", "Field updated successfully");
                } else {
                    Log.d("UserProfileFragment", "Failed to update field: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d("UserProfileFragment", "Error updating field: " + t.getMessage());
            }
        };
    }

    private void setupAllCarousels(View view) {
        int[] carouselIds = {
                R.id.favouriteEventsCarousel,
                R.id.favouriteServicesCarousel,
                R.id.favouriteProductsCarousel
        };

        for (int id : carouselIds) {
            setupCarousel(view, id);
        }
    }

    private void setupCarousel(View view, int carouselId) {
        RecyclerView carousel = view.findViewById(carouselId);
        LinearLayoutManager layoutManager = new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false);
        carousel.setLayoutManager(layoutManager);
        carousel.setAdapter(new SimpleCarouselAdapter(R.drawable.image_placeholder, 4));
    }

    private void fillFields(View view) {
        Integer userId = JwtUtil.extractId(requireContext());
        String authHeader = JwtUtil.getAuthorizationValue(requireContext());

        if (role == UserRole.EVENT_ORG) {
            userService.getOrganizer(authHeader, userId).enqueue((Callback<GetOrganizerResponseDTO>) createFillCallback(view, true));
        } else if (role == UserRole.SPP) {
            userService.getProvider(authHeader, userId).enqueue((Callback<GetProviderResponseDTO>) createFillCallback(view, false));
        }
    }

    private Callback<?> createFillCallback(View view, boolean isOrganizer) {
        return new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    if (isOrganizer) {
                        fillFields(view, (GetOrganizerResponseDTO) response.body(), null);
                    } else {
                        fillFields(view, null, (GetProviderResponseDTO) response.body());
                    }
                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                Log.d("UserProfileFragment", Objects.requireNonNull(t.getMessage()));
            }
        };
    }

    private void fillFields(View view, GetOrganizerResponseDTO orgDto, GetProviderResponseDTO proDto) {
        if (orgDto != null) {
            populateFields(view, orgDto.getEmail(), orgDto.getPhone(), orgDto.getLocation().getCountry(),
                    orgDto.getLocation().getCity(), orgDto.getLocation().getStreet(), orgDto.getLocation().getHouseNumber(),
                    orgDto.getPhotoURL());

            fillField(view, R.id.change_first_name, orgDto.getFirstName());
            fillField(view, R.id.change_last_name, orgDto.getLastName());
        } else if (proDto != null) {
            populateFields(view, proDto.getEmail(), proDto.getPhone(), proDto.getLocation().getCountry(),
                    proDto.getLocation().getCity(), proDto.getLocation().getStreet(), proDto.getLocation().getHouseNumber(),
                    proDto.getPhotoURL());

            fillField(view, R.id.change_org_name, proDto.getOrgName());
            fillField(view, R.id.change_org_desc, proDto.getOrgDesc());
        }
    }

    private void populateFields(View view, String email, String phone, String country, String city, String street, String houseNumber, String photoURL) {
        fillField(view, R.id.email_user_profile, email);
        fillField(view, R.id.change_phone, phone);
        fillField(view, R.id.change_country, country);
        fillField(view, R.id.change_city, city);
        fillField(view, R.id.change_street, street);
        fillField(view, R.id.change_house_number, houseNumber);
        fillField(view, R.id.change_profile_pic, photoURL);

        ShapeableImageView profilePic = view.findViewById(R.id.profile_picture);
        Picasso.get()
                .load(photoURL)
                .placeholder(R.drawable.image_placeholder)
                .error(R.drawable.image_placeholder)
                .into(profilePic);
    }

    private void fillField(View view, int fieldId, String data) {
        TextInputEditText field = view.findViewById(fieldId);
        field.setText(data);
    }

    private void showChangePasswordDialog(Context context) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_change_password, null);
        TextInputEditText etOldPassword = dialogView.findViewById(R.id.et_old_password);
        TextInputEditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        TextInputEditText etConfirmPassword = dialogView.findViewById(R.id.et_confirm_password);
        MaterialTextView errorMsg = dialogView.findViewById(R.id.change_pass_error);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .setPositiveButton(R.string.change_pass, null) // Set to null to override default behavior
                .setNegativeButton(R.string.cancel, (d, which) -> d.dismiss())
                .create();

        dialog.show();

        // Handle positive button click explicitly
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String oldPassword = Objects.requireNonNull(etOldPassword.getText()).toString().trim();
            String newPassword = Objects.requireNonNull(etNewPassword.getText()).toString().trim();
            String confirmPassword = Objects.requireNonNull(etConfirmPassword.getText()).toString().trim();

            if (oldPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                errorMsg.setText(R.string.all_fields_are_required);
                errorMsg.setVisibility(View.VISIBLE);
                return;
            }
            if (!newPassword.equals(confirmPassword)) {
                errorMsg.setText(R.string.new_passwords_do_not_match);
                errorMsg.setVisibility(View.VISIBLE);
                return; // Keep the dialog open
            }

            // Proceed with password update logic
            if (role == UserRole.EVENT_ORG) {
                UpdateOrganizerRequestDTO dto = new UpdateOrganizerRequestDTO();
                dto.setOldPassword(oldPassword);
                dto.setNewPassword1(newPassword);
                dto.setNewPassword2(confirmPassword);
                userService.updateOrganizer(JwtUtil.getAuthorizationValue(requireContext()), JwtUtil.extractId(requireContext()), dto)
                        .enqueue((Callback<UpdateOrganizerResponseDTO>) createPasswordChangeCallback(errorMsg, dialog));
            }
            if (role == UserRole.SPP) {
                UpdateProviderRequestDTO dto = new UpdateProviderRequestDTO();
                dto.setOldPassword(oldPassword);
                dto.setNewPassword1(newPassword);
                dto.setNewPassword2(confirmPassword);
                userService.updateProvider(JwtUtil.getAuthorizationValue(requireContext()), JwtUtil.extractId(requireContext()), dto)
                        .enqueue((Callback<UpdateProviderResponseDTO>) createPasswordChangeCallback(errorMsg, dialog));
            }
        });
    }

    private Callback<?> createPasswordChangeCallback(MaterialTextView errorMsg, AlertDialog dialog) {
        return new Callback<Object>() {
            @Override
            public void onResponse(Call<Object> call, Response<Object> response) {
                if (response.isSuccessful()) {
                    dialog.dismiss(); // Close dialog on success
                    errorMsg.setVisibility(View.GONE);

                } else {
                    errorMsg.setText(response.message());
                    errorMsg.setVisibility(View.VISIBLE);

                }
            }
            @Override
            public void onFailure(Call<Object> call, Throwable t) {
                errorMsg.setText(t.getMessage());
                errorMsg.setVisibility(View.VISIBLE);
            }
        };
    }

}
