package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.auth.RegisterRequestDTO;
import rs.ac.uns.eventplanner.team7.model.Location;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.AuthService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;

public class RegistrationActivity extends AppCompatActivity {

    private String token; // used for auth user registration
    private String selectedRole;
    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        authService = ClientUtils.retrofit.create(AuthService.class);
        setupRadio();
        setupLoginNavigation();
        MaterialButton regButton = findViewById(R.id.register_button);
        regButton.setOnClickListener(v -> registerOnClick());
        checkRedirection();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkRedirection();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkRedirection();
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
        signIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void setupRadio(){
        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
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
            }
        });
    }

    public void registerOnClick() {
        String country = validateRequiredField(R.id.country, "Country is required");
        String city = validateRequiredField(R.id.city, "City is required");
        String street = validateRequiredField(R.id.street, "Street is required");
        String houseNumber = validateRequiredField(R.id.house_number, "House number is required");
        String email = validateRequiredField(R.id.email, "Email is required");
        String pass1 = validateRequiredField(R.id.password, "Password is required");
        String pass2 = validateRequiredField(R.id.password_confirm, "Password confirmation is required");
        String phone = validateRequiredField(R.id.phone_number, "Phone number is required");
        TextInputEditText photoInput = findViewById(R.id.photo_url);
        String photoURL = Objects.requireNonNull(photoInput.getText()).toString().trim();
        String orgName = "";
        String orgDesc = "";
        String firstName = "";
        String lastName = "";

        if (country == null || city == null || street == null || houseNumber == null ||
                email == null || pass1 == null || pass2 == null) {
            return;
        }

        if (selectedRole.equals("SPP")) {
            orgName = validateRequiredField(R.id.org_name, "Organization name is required");
            orgDesc = validateRequiredField(R.id.org_desc, "Organization description is required");
            if (orgName == null || orgDesc == null) {
                return;
            }
        } else if (selectedRole.equals("EVENT_ORG")) {
            firstName = validateRequiredField(R.id.first_name, "First name is required");
            lastName = validateRequiredField(R.id.last_name, "Last name is required");
            if (firstName == null || lastName == null) {
                return;
            }
        }

        if (!validatePassword(pass1, pass2)){
            return;
        }
        if (!validateEmail(email)) {
            return;
        }
        if (!validatePhone(phone)) {
            return;
        }

        UserRole role = Enum.valueOf(UserRole.class, selectedRole);
        Location loc = new Location(0, 0 , country, city, street, houseNumber);
        RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(email, pass1, pass2, role, photoURL, phone, loc,
                firstName, lastName, orgName, orgDesc);

        authService.register(registerRequestDTO).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
                    startActivity(intent);
                } else {
                    finish();
                    startActivity(getIntent());
                    Toast.makeText(RegistrationActivity.this, "Error during the registration", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(RegistrationActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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

    private void checkRedirection() {
        Uri data = getIntent().getData();
        if (data != null) {
            String email = data.getQueryParameter("email");
            String token = data.getQueryParameter("token");
            if (email != null && token != null)  {
                TextInputEditText emailInput = findViewById(R.id.email);
                emailInput.setText(email);
                this.token = token;
                // TODO handle auth user registration case here
            }
        }
    }
}