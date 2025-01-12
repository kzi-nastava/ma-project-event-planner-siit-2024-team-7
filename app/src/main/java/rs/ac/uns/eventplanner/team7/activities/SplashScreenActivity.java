package rs.ac.uns.eventplanner.team7.activities;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Timer;
import java.util.TimerTask;

import rs.ac.uns.eventplanner.team7.R;
import rs.ac.uns.eventplanner.team7.utils.JwtUtil;

public class SplashScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        JwtUtil.clearCity(this);
        JwtUtil.clearRole(this);
        JwtUtil.clearToken(this);

        int SPLASH_TIME_OUT = 500;
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}