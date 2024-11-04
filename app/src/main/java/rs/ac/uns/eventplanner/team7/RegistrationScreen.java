package rs.ac.uns.eventplanner.team7;

import android.os.Bundle;
import android.widget.RadioButton;

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
        RadioButton eoradio = findViewById(R.id.eoradio);
        if (!eoradio.isChecked()) { // need to display the registration for SPP

        }
    }
}