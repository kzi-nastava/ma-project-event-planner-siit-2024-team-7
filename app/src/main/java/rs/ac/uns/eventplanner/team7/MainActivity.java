package rs.ac.uns.eventplanner.team7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import rs.ac.uns.eventplanner.team7.RegistrationScreen;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import rs.ac.uns.eventplanner.team7.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
        Button regButton = findViewById(R.id.reg_button);
        regButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an Intent to start RegisterActivity
                Intent intent = new Intent(MainActivity.this, RegistrationScreen.class);
                startActivity(intent);
            }
        });

    }
}