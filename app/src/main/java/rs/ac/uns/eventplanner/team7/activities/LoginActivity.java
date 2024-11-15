package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.util.Objects;

import rs.ac.uns.eventplanner.team7.R;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MaterialTextView signUp = findViewById(R.id.signupLink);
        signUp.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        MaterialButton loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(v -> {
            TextInputEditText usernameInput = findViewById(R.id.usernameInput);
            TextInputEditText passwordInput = findViewById(R.id.passwordInput);
            if (Objects.requireNonNull(usernameInput.getText()).toString().equals("admin@ep.com")
                    && Objects.requireNonNull(passwordInput.getText()).toString().equals("1234")) {
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);
                return;
            }
            TextInputLayout usernameLayout = findViewById(R.id.usernameInputLayout);
            TextInputLayout passwordLayout = findViewById(R.id.passwordInputLayout);
            usernameLayout.setError("Username is not valid");
            passwordLayout.setError("Password is not valid");
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        TextInputLayout usernameLayout = findViewById(R.id.usernameInputLayout);
        TextInputLayout passwordLayout = findViewById(R.id.passwordInputLayout);
        usernameLayout.setError(null);
        passwordLayout.setError(null);
        TextInputEditText usernameInput = findViewById(R.id.usernameInput);
        TextInputEditText passwordInput = findViewById(R.id.passwordInput);
        usernameInput.setText("");
        passwordInput.setText("");
        usernameInput.clearFocus();
        passwordInput.clearFocus();
    }
}