package rs.ac.uns.eventplanner.team7.ui.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import java.time.Instant;
import java.util.Date;

import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;
import rs.ac.uns.eventplanner.team7.utils.AuthUtil;

public class RoutingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);
        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true);

        Intent intent = getIntentForNextActivity();
        final int SPLASH_TIME_OUT = 500;
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(intent);
            finish();
        }, SPLASH_TIME_OUT);
    }

    @NonNull
    private Intent getIntentForNextActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        Uri data = getIntent().getData();
        if (data != null && data.getPath() != null)
            intent = handleRedirection(data, intent);

        Instant suspensionEnd = AuthUtil.getSuspensionEnd(this);
        if (suspensionEnd != null) {
            if (suspensionEnd.isBefore(Instant.now())) AuthUtil.clearPreferences(this);
            return intent;
        }

        if (AuthUtil.getToken(this) != null && isTokenExpired()) {
            // bundle can contain values if handling invitation redirection
            Bundle params = intent.getExtras() != null ? intent.getExtras().deepCopy() : new Bundle();
            params.putBoolean("sessionExpired", true);
            intent = new Intent(this, LoginActivity.class).putExtras(params);
            AuthUtil.clearPreferences(this);
        }
        return intent;
    }

    @NonNull
    private Intent handleRedirection(Uri data, Intent defaultIntent) {
        String path = data.getPath().substring(1); // skips leading '/'
        switch (path) {
            case "quick_registration":
                return handleQuickRegistration(data, defaultIntent);

            case "accept_invitation":
                return handleInvitation(data, defaultIntent);

            default:
                return defaultIntent;
        }
    }

    @NonNull
    private Intent handleQuickRegistration(Uri data, Intent defaultIntent) {
        String email = data.getQueryParameter("email");
        String token = data.getQueryParameter("token");
        if (email == null || token == null) return defaultIntent;
        Bundle params = new Bundle();
        params.putString("email", email);
        params.putString("token", token);
        AuthUtil.clearPreferences(this);
        return new Intent(this, RegistrationActivity.class).putExtras(params);
    }

    @NonNull
    private Intent handleInvitation(Uri data, Intent targetIntent) {
        String email = data.getQueryParameter("email");
        String token = data.getQueryParameter("token");
        String eventId = data.getQueryParameter("eventId");
        if (email == null || token == null || eventId == null) return targetIntent;

        try {
            // if not logged in go to login first
            if (AuthUtil.extractRole(this) == UserRole.GUEST) {
                targetIntent = new Intent(this, LoginActivity.class);
            }
            Bundle params = new Bundle();
            params.putString("email", email);
            params.putString("token", token);
            params.putInt("eventId", Integer.parseInt(eventId));
            return targetIntent.putExtras(params);
        } catch (NumberFormatException e) {
            return targetIntent;
        }
    }

    private boolean isTokenExpired() {
        Date expirationDate = AuthUtil.extractExpirationDate(this);
        return expirationDate == null || expirationDate.before(new Date());
    }
}
