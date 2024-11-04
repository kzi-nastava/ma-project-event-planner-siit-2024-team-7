package rs.ac.uns.eventplanner.team7;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

public class HomeScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);
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
    public void registrationOnClick(View view) {
        Intent intent = new Intent(HomeScreen.this, RegistrationScreen.class);
        startActivity(intent);
    }

    public void loginBtnOnClick(View view) {
        Intent intent = new Intent(HomeScreen.this, LoginScreen.class);
        startActivity(intent);
        finish();
    }

}