package rs.ac.uns.eventplanner.team7.activities;

import static android.widget.Toast.LENGTH_SHORT;
import static rs.ac.uns.eventplanner.team7.utils.ClientUtils.retrofit;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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

public class RegistrationActivity extends AppCompatActivity {

    private String authToken;
    private String selectedRole;
    private final Location address = new Location();
    private final AuthService authService = retrofit.create(AuthService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        setupRadio();
        setupLoginNavigation();
        MaterialButton regButton = findViewById(R.id.register_button);
        regButton.setOnClickListener(v -> registerOnClick());
        checkRedirection(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkRedirection(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRedirection(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void setupLoginNavigation() {
        TextView signIn = findViewById(R.id.sign_in_reg);
        signIn.setOnClickListener(v -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }

    private void setupRadio(){
        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rb_service_provider) { // if spp is selected
                findViewById(R.id.eoinputs).setVisibility(View.GONE);
                findViewById(R.id.sppinputs).setVisibility(View.VISIBLE);
                MaterialButton regButton = findViewById(R.id.register_button);
                selectedRole = "SPP";
                regButton.setEnabled(true);
                regButton.setText(R.string.register_spp);
            }
            if (checkedId == R.id.rb_event_organizer) { // if eo is selected
                findViewById(R.id.eoinputs).setVisibility(View.VISIBLE);
                findViewById(R.id.sppinputs).setVisibility(View.GONE);
                MaterialButton regButton = findViewById(R.id.register_button);
                selectedRole = "EVENT_ORG";
                regButton.setEnabled(true);
                regButton.setText(R.string.register_eo);

            }
            if (checkedId == R.id.rb_auth) {
                MaterialButton regButton = findViewById(R.id.register_button);
                selectedRole = "AUTH";
                regButton.setEnabled(true);
                regButton.setText(R.string.register_eo);
            }
        });
    }

    public void registerOnClick() {
        String country = validateRequiredField(R.id.country, "Country is required");
        address.setCountry(validateRequiredField(R.id.country, "Country is required"));
        String city = validateRequiredField(R.id.city, "City is required");
        address.setCity(city);
        String street = validateRequiredField(R.id.street, "Street is required");
        address.setStreet(street);
        String houseNumber = validateRequiredField(R.id.house_number, "House number is required");
        address.setHouseNumber(houseNumber);
        String email = validateRequiredField(R.id.email, "Email is required");
        String pass1 = validateRequiredField(R.id.password, "Password is required");
        String pass2 = validateRequiredField(R.id.password_confirm, "Password confirmation is required");
        String phone = validateRequiredField(R.id.phone_number, "Phone number is required");
        TextInputEditText photoInput = findViewById(R.id.photo_url);
        String photoURL = Objects.requireNonNull(photoInput.getText()).toString().trim();

        if (country == null || city == null || street == null || houseNumber == null ||
                email == null || pass1 == null || pass2 == null) return;

        if (!validatePassword(pass1, pass2)) return;
        if (!validateEmail(email)) return;
        if (!validatePhone(phone)) return;

        RegisterRequestDTO dto = prepareRequest(email, pass1, pass2, photoURL, phone);
        if (dto == null) return;
        authService.register(dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                    startActivity(getIntent());
                    Toast.makeText(RegistrationActivity.this, "Error during the registration",
                            LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network error: " + t.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    private boolean validatePassword(String pass1, String pass2) {
        TextInputEditText password1Input = findViewById(R.id.password);
        TextInputEditText password2Input = findViewById(R.id.password_confirm);
        if (!pass1.equals(pass2)) {
            password1Input.setText("");
            password2Input.setText("");
            password1Input.setError("Passwords do NOT match");
            password2Input.setError("Passwords do NOT match");
            return false;
        }
        if (pass1.length() < 8) {
            password1Input.setText("");
            password2Input.setText("");
            password1Input.setError("Password must be minimum 8 characters long");
            return false;
        }
        return true;
    }

    private boolean validateEmail(String email) {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            TextInputEditText emailInput = findViewById(R.id.email);
            emailInput.setText("");
            emailInput.setError("Invalid email format");
            return false;
        }
        return true;
    }

    private String validateRequiredField(int viewId, String errorMessage) {
        TextInputEditText input = findViewById(viewId);
        String text = Objects.requireNonNull(input.getText()).toString().trim();

        if (text.isEmpty()) {
            input.setError(errorMessage);
            return null;
        }
        return text;
    }

    private boolean validatePhone(String phone) {
        if (!Patterns.PHONE.matcher(phone).matches()) {
            TextInputEditText phoneInput = findViewById(R.id.phone_number);
            phoneInput.setText("");
            phoneInput.setError("Invalid phone format");
            return false;
        }
        return true;
    }

    private void checkRedirection(boolean validateToken) {
        Uri data = getIntent().getData();
        if (data != null) {
            String email = data.getQueryParameter("email");
            String token = data.getQueryParameter("token");
            if (email == null || token == null) {
                Toast.makeText(RegistrationActivity.this,
                        "Missing required information. Redirecting to login", LENGTH_SHORT).show();
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            TextInputEditText emailInput = findViewById(R.id.email);
            emailInput.setText(email);
            this.authToken = token;
            if (!validateToken) return;
            validateToken(email);
            selectAuthButton();
        }
    }

    private void validateToken(String email) {
        ValidateQuickRegistrationDTO dto = new ValidateQuickRegistrationDTO(email, this.authToken);
        authService.validateQuickRegistration(dto).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) Toast.makeText(RegistrationActivity.this,
                            "Token is valid", LENGTH_SHORT).show();
                else {
                    Toast.makeText(RegistrationActivity.this,
                            "Invalid auth token. Redirecting to login", LENGTH_SHORT).show();
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network error: " + t.getMessage(),
                        LENGTH_SHORT).show();
            }
        });
    }

    @Nullable
    private RegisterRequestDTO prepareRequest(String email, String pass1, String pass2,
                                              String photoURL, String phone) {
        switch (selectedRole) {
            case "SPP":
                String orgName = validateRequiredField(R.id.org_name,
                        "Organization name is required");
                String orgDesc = validateRequiredField(R.id.org_desc,
                        "Organization description is required");
                if (orgName == null || orgDesc == null) return null;
                return new RegisterRequestDTO(email, pass1, pass2, UserRole.SPP, photoURL, phone,
                        address, orgName, orgDesc);
            case "EVENT_ORG":
                String firstName = validateRequiredField(R.id.first_name,
                        "First name is required");
                String lastName = validateRequiredField(R.id.last_name,
                        "Last name is required");
                if (firstName == null || lastName == null) return null;
                return new RegisterRequestDTO(email, pass1, pass2, UserRole.SPP, photoURL, phone,
                        address, firstName, lastName);
            case "AUTH": return new RegisterRequestDTO(email, pass1, pass2, photoURL, phone,
                    address, authToken);
            default: return new RegisterRequestDTO(); // will never occur
        }
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