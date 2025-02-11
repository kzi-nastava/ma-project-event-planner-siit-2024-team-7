package rs.ac.uns.eventplanner.team7.activities;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.auth.RegisterRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.ValidateQuickRegistrationDTO;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.AuthService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class RegistrationActivity extends AppCompatActivity {
    private final RegisterRequestDTO registerRequest = new RegisterRequestDTO();
    private final AuthService authService = ClientUtils.injectService(AuthService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupRadio();
        setupLoginNavigation();
        MaterialButton regButton = findViewById(R.id.register_button);
        regButton.setOnClickListener(v -> registerOnClick());
        JwtUtil.setDefaultValues(this);
        checkRedirection();
    }

    private void setupLoginNavigation() {
        TextView signIn = findViewById(R.id.sign_in_reg);
        signIn.setOnClickListener(v -> switchToLoginActivity());
    }

    private void switchToLoginActivity() {
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    private void setupRadio(){
        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_service_provider) { // if spp is selected
                findViewById(R.id.eoinputs).setVisibility(View.GONE);
                findViewById(R.id.sppinputs).setVisibility(View.VISIBLE);
                MaterialButton regButton = findViewById(R.id.register_button);
                registerRequest.setRole(UserRole.SPP);
                regButton.setEnabled(true);
                regButton.setText(R.string.register_spp);
            }
            if (checkedId == R.id.rb_event_organizer) { // if eo is selected
                findViewById(R.id.eoinputs).setVisibility(View.VISIBLE);
                findViewById(R.id.sppinputs).setVisibility(View.GONE);
                MaterialButton regButton = findViewById(R.id.register_button);
                registerRequest.setRole(UserRole.EVENT_ORG);
                regButton.setEnabled(true);
                regButton.setText(R.string.register_eo);

            }
            if (checkedId == R.id.rb_auth) {
                MaterialButton regButton = findViewById(R.id.register_button);
                registerRequest.setRole(UserRole.AUTH);
                regButton.setEnabled(true);
                regButton.setText(R.string.register_auth);
            }
        });
    }

    public void registerOnClick() {
        final Location address = registerRequest.getLocation();
        address.setCountry(validateRequiredField(R.id.country, R.string.country_is_required));
        address.setCity(validateRequiredField(R.id.city, R.string.city_is_required));
        address.setStreet(validateRequiredField(R.id.street, R.string.street_is_required));
        address.setHouseNumber(validateRequiredField(R.id.house_number, R.string.house_number_is_required));
        registerRequest.setEmail(validateRequiredField(R.id.email, R.string.email_is_required));
        registerRequest.setPassword(validateRequiredField(R.id.password, R.string.password_is_required));
        registerRequest.setPassword2(validateRequiredField(R.id.password_confirm, R.string.password_confirmation_is_required));
        registerRequest.setPhone(validateRequiredField(R.id.phone_number, R.string.phone_number_is_required));
        TextInputEditText photoInput = findViewById(R.id.photo_url);
        registerRequest.setPhotoURL(photoInput.getText() == null ? "" : photoInput.getText().toString().trim());

        if (!registerRequest.areValidFields() || !validateRoleSpecificFields()) return;
        if (!validatePassword()) return;
        if (!validateEmail()) return;
        if (!validatePhone()) return;

        authService.register(registerRequest).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(RegistrationActivity.this,
                            R.string.registration_successful, LENGTH_LONG).show();
                    switchToLoginActivity();
                } else {
                    Toast.makeText(RegistrationActivity.this,
                            R.string.error_during_registration, LENGTH_SHORT).show();
                    finish();
                    startActivity(getIntent());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Error: " + t.getMessage(),
                        LENGTH_LONG).show();
            }
        });
    }

    private boolean validateRoleSpecificFields() {
        switch (registerRequest.getRole()) {
            case SPP:
                String orgName = validateRequiredField(R.id.org_name, R.string.organization_name_is_required);
                String orgDesc = validateRequiredField(R.id.org_desc, R.string.organization_description_is_required);
                registerRequest.setOrgName(orgName);
                registerRequest.setOrgDesc(orgDesc);
                return orgName != null && orgDesc != null;
            case EVENT_ORG:
                String firstName = validateRequiredField(R.id.first_name, R.string.first_name_is_required);
                String lastName = validateRequiredField(R.id.last_name, R.string.last_name_is_required);
                registerRequest.setFirstName(firstName);
                registerRequest.setLastName(lastName);
                return firstName != null && lastName != null;
        }
        return true;
    }

    private boolean validatePassword() {
        TextInputEditText password1Input = findViewById(R.id.password);
        TextInputEditText password2Input = findViewById(R.id.password_confirm);
        if (!registerRequest.getPassword().equals(registerRequest.getPassword2())) {
            password1Input.setText("");
            password2Input.setText("");
            password1Input.setError(getString(R.string.passwords_do_not_match));
            password2Input.setError(getString(R.string.passwords_do_not_match));
            return false;
        }
        if (registerRequest.getPassword().length() < 8) {
            password1Input.setText("");
            password2Input.setText("");
            password1Input.setError(getString(R.string.password_min_length_error));
            return false;
        }
        return true;
    }

    private boolean validateEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(registerRequest.getEmail()).matches()) {
            TextInputEditText emailInput = findViewById(R.id.email);
            emailInput.setText("");
            emailInput.setError(getString(R.string.invalid_email_format));
            return false;
        }
        return true;
    }

    private String validateRequiredField(@IdRes int viewId, @StringRes int errorMessage) {
        TextInputEditText input = findViewById(viewId);
        String text = Objects.requireNonNull(input.getText()).toString().trim();

        if (text.isEmpty()) {
            input.setError(getString(errorMessage));
            return null;
        }
        return text;
    }

    private boolean validatePhone() {
        if (!Patterns.PHONE.matcher(registerRequest.getPhone()).matches()) {
            TextInputEditText phoneInput = findViewById(R.id.phone_number);
            phoneInput.setText("");
            phoneInput.setError(getString(R.string.invalid_phone_format));
            return false;
        }
        return true;
    }

    private void checkRedirection() {
        Bundle data = getIntent().getExtras();
        if (data == null) return;
        TextInputEditText emailInput = findViewById(R.id.email);
        String email = data.getString("email");
        emailInput.setText(email);
        emailInput.setEnabled(false);
        registerRequest.setEmail(email);
        validateToken(data.getString("token"));
        selectAuthButton();
    }

    private void validateToken(String authToken) {
        ValidateQuickRegistrationDTO dto =
                new ValidateQuickRegistrationDTO(registerRequest.getEmail(), authToken);
        authService.validateQuickRegistration(dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    registerRequest.setAuthToken(authToken);
                    Toast.makeText(RegistrationActivity.this, R.string.token_is_valid, LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RegistrationActivity.this, R.string.invalid_auth_token, LENGTH_LONG).show();
                    switchToLoginActivity();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Error: " + t.getMessage(),
                        LENGTH_LONG).show();
            }
        });
    }

    private void selectAuthButton() {
        RadioButton radioButtonAuth = findViewById(R.id.rb_auth);
        radioButtonAuth.setVisibility(View.VISIBLE);
        radioButtonAuth.toggle();
        RadioButton radioButtonEO = findViewById(R.id.rb_event_organizer);
        radioButtonEO.setVisibility(View.GONE);
        RadioButton radioButtonSPP = findViewById(R.id.rb_service_provider);
        radioButtonSPP.setVisibility(View.GONE);
    }
}