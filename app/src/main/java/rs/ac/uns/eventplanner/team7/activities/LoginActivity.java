package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import javax.crypto.EncryptedPrivateKeyInfo;

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

    private AuthService authService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = ClientUtils.retrofit.create(AuthService.class);
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
            intent.putExtra("isGuest", true);
            startActivity(intent);
        });

        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> handleLogin());
    }

    private void handleLogin() {
        TextInputEditText emailInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }

        LoginRequestDTO loginRequest = new LoginRequestDTO(email, password);

        Call<LoginResponseDTO> call = authService.login(loginRequest);
        call.enqueue(new Callback<LoginResponseDTO>() {
            @Override
            public void onResponse(Call<LoginResponseDTO> call, Response<LoginResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponseDTO loginResponse = response.body();
                    String token = loginResponse.getToken();
                    Integer userId = loginResponse.getId();
                    UserRole role = loginResponse.getRole();
                    JwtUtil.saveToken(LoginActivity.this, token);
                    startActivity(new Intent(LoginActivity.this, HomeActivity.class));
                } else {
                    clearFocus();
                    MaterialTextView errorMsg = findViewById(R.id.error_login);
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<LoginResponseDTO> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void clearFocus() {
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        usernameInput.setText("");
        passwordInput.setText("");
        usernameInput.clearFocus();
        passwordInput.clearFocus();
    }

}