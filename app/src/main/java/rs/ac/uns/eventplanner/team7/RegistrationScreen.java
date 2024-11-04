package rs.ac.uns.eventplanner.team7;

import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class RegistrationScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_screen);
        RadioGroup radioGroup = findViewById(R.id.register_rg);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                    if (checkedId == 1) { // if spp is selected
                        findViewById(R.id.eoinputs).setVisibility(View.GONE);
                        findViewById(R.id.sppinputs).setVisibility(View.VISIBLE);
                    }
                    if (checkedId == 0) { // if eo is selected
                        findViewById(R.id.eoinputs).setVisibility(View.VISIBLE);
                        findViewById(R.id.sppinputs).setVisibility(View.GONE);
                    }
            }
        });
    }
}