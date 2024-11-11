package rs.ac.uns.eventplanner.team7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import rs.ac.uns.eventplanner.team7.activities.LoginActivity;

public class RegistrationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == R.id.rb_service_provider) { // if spp is selected
                        findViewById(R.id.eoinputs).setVisibility(View.GONE);
                        findViewById(R.id.sppinputs).setVisibility(View.VISIBLE);
                    }
                    if (checkedId == R.id.rb_event_organizer) { // if eo is selected
                        findViewById(R.id.eoinputs).setVisibility(View.VISIBLE);
                        findViewById(R.id.sppinputs).setVisibility(View.GONE);
                    }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
    }

    @Override
    protected void onResume() {
        super.onResume();
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
}