package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import rs.ac.uns.eventplanner.team7.R;

public class RegistrationActivity extends AppCompatActivity {

    private String token; // used for auth user registration

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        checkRedirection();

        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
                if (checkedId == R.id.rb_service_provider) { // if spp is selected
                    findViewById(R.id.eoinputs).setVisibility(View.GONE);
                    findViewById(R.id.sppinputs).setVisibility(View.VISIBLE);
                    MaterialButton regButton = findViewById(R.id.register_button);
                    regButton.setEnabled(true);
                    regButton.setText(R.string.register_spp);
                }
                if (checkedId == R.id.rb_event_organizer) { // if eo is selected
                    findViewById(R.id.eoinputs).setVisibility(View.VISIBLE);
                    findViewById(R.id.sppinputs).setVisibility(View.GONE);
                    MaterialButton regButton = findViewById(R.id.register_button);
                    regButton.setEnabled(true);
                    regButton.setText(R.string.register_eo);
                }
        });

        TextView signIn = findViewById(R.id.sign_in_reg);
        signIn.setOnClickListener(view -> {
            Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
            startActivity(intent);
        });
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

    public void registerOnClick(View view) {
        Toast.makeText(this, "Successful registration!", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(RegistrationActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
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