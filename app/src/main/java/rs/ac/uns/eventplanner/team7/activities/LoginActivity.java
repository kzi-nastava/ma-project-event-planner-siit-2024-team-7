package rs.ac.uns.eventplanner.team7.activities;

import static android.widget.Toast.LENGTH_LONG;
import static android.widget.Toast.LENGTH_SHORT;

import android.content.Intent;
import android.net.Uri;
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
import rs.ac.uns.eventplanner.team7.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.invitation.InvitationAcceptanceDTO;
import rs.ac.uns.eventplanner.team7.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.services.AuthService;
import rs.ac.uns.eventplanner.team7.services.InvitationService;
import rs.ac.uns.eventplanner.team7.utils.ClientUtils;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class LoginActivity extends AppCompatActivity {

    private final AuthService authService = ClientUtils.injectService(AuthService.class);
    private final InvitationService invitationService = ClientUtils.injectService(InvitationService.class);
    private InvitationAcceptanceDTO invitationDto;

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
        if (invitationDto == null) checkRedirection();
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
            JwtUtil.saveCity(LoginActivity.this, "all");
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
            Toast.makeText(this, R.string.enter_email_and_password, LENGTH_SHORT).show();
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
                    if (invitationDto != null && email.equals(invitationDto.getEmail())) {
                        handleInvitationAccepting();
                        return;
                    }
                    switchToHomeActivity();

                } else {
                    clearFocus();
                    MaterialTextView errorMsg = findViewById(R.id.error_login);
                    errorMsg.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<LoginResponseDTO> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error: " + t.getMessage(), LENGTH_SHORT).show();
            }
        });
    }

    private void switchToHomeActivity() {
        startActivity(new Intent(LoginActivity.this, HomeActivity.class));
        finish();
    }

    private void handleInvitationAccepting() {
        String bearerToken = JwtUtil.getAuthorizationValue(this);
        invitationService.acceptInvitation(bearerToken, invitationDto)
                .enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseMessageDTO> call, @NonNull Response<ResponseMessageDTO> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(LoginActivity.this,
                            Objects.requireNonNull(response.body()).getMessage(), LENGTH_LONG).show();
                }
                if (response.code() == 400) {
                    Toast.makeText(LoginActivity.this,
                            Objects.requireNonNull(response.body()).getMessage(), LENGTH_SHORT).show();
                } else if (response.code() == 404) {
                    Toast.makeText(LoginActivity.this, "Invitation not found!",
                            LENGTH_SHORT).show();
                }
                switchToHomeActivity();
            }

            @Override
            public void onFailure(@NonNull Call<ResponseMessageDTO> call, @NonNull Throwable t) {
                Toast.makeText(LoginActivity.this, "Error accepting invitation!", LENGTH_SHORT).show();
                switchToHomeActivity();
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

    /// Redirection can occur when accepting invitations
    private void checkRedirection() {
        Uri data = getIntent().getData();
        if (data == null) return;
        JwtUtil.clearCity(this);
        JwtUtil.clearRole(this);
        JwtUtil.clearToken(this);
        try {
            String email = data.getQueryParameter("email");
            Integer eventId = Integer.parseInt(data.getQueryParameter("eventId"));
            String token = data.getQueryParameter("token");
            if (email == null || token == null) return;
            TextInputEditText emailInput = findViewById(R.id.usernameInput);
            emailInput.setText(email);
            invitationDto = new InvitationAcceptanceDTO(email, eventId, token);
        } catch (NumberFormatException ignored) {}
    }

}