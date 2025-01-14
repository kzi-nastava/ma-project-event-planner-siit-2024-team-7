package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginResponseDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.AuthService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class LoginActivity extends AppCompatActivity {

    private final AuthService authService = ClientUtils.injectService(AuthService.class);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setupNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextInputLayout usernameLayout = findViewById(R.id.usernameInputLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordInputLayout);
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        clearFocus();
        checkRedirection();
    }

    private void setupNavigation() {
        MaterialTextView signUp = findViewById(R.id.signupLink);
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        MaterialTextView guestButton = findViewById(R.id.continue_as_guest);
        guestButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(intent);
            finish();
        });

        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        TextInputEditText emailInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        String email = Objects.requireNonNull(emailInput.getText()).toString().trim();
        String password = Objects.requireNonNull(passwordInput.getText()).toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, R.string.enter_email_and_password, Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        Call<LoginResponseDTO> call = authService.login(loginRequest);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<LoginResponseDTO> call,
                                   @NonNull Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();
                    String token = loginResponse.getToken();
                    UserRole role = loginResponse.getRole();
                    JwtUtil.saveToken(LoginActivity.this, token);
                    JwtUtil.saveRole(LoginActivity.this, role.toString());
                    JwtUtil.saveCity(LoginActivity.this, loginResponse.getCity());
                    switchToHomeActivity();
                } else {
                    clearFocus();
                    MaterialTextView errorMsg = findViewById(R.id.error_login);
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void switchToHomeActivity() {
        Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
        Bundle params = getIntent().getExtras();
        if (params != null) {
            intent.putExtras(params);
        }
        startActivity(intent);
        finish();
    }

    private void clearFocus() {
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        usernameInput.setText("");
        passwordInput.setText("");
        usernameInput.clearFocus();
        passwordInput.clearFocus();
    }

    /// Redirection can occur when accepting invitations
    private void checkRedirection() {
        Bundle data = getIntent().getExtras();
        if (data == null) return;
        String email = data.getString("email");
        TextInputEditText emailInput = findViewById(R.id.usernameInput);
        emailInput.setText(email);
    }
}