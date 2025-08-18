package rs.ac.uns.eventplanner.team7.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.data.model.enums.UserRole;

public class AuthUtil {

    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String SUSPENSION_END_KEY = "suspension_end";

    public static void saveToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public static void saveSuspensionEnd(Context context, String suspensionEnd) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SUSPENSION_END_KEY, suspensionEnd);
        editor.apply();
    }

    public static void clearPreferences(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().clear().apply();
    }

    public static UserRole extractRole(Context context) {
        String extractedRole = extractClaim(context, "role", String.class);
        return extractedRole == null ? UserRole.GUEST : UserRole.valueOf(extractedRole);
    }

    public static String extractCity(Context context) {
        String city = extractClaim(context, "city", String.class);
        return city != null ? city : "All";
    }

    public static Integer extractId(Context context) {
        return extractClaim(context, "id", Integer.class);
    }

    public static Date extractExpirationDate(Context context) {
        return extractClaim(context, "exp", Date.class);
    }

    public static String extractEmail(Context context) {
        return extractClaim(context, "sub", String.class);
    }

    public static boolean isSuspended(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        var suspensionEnd = sharedPreferences.getString(SUSPENSION_END_KEY, null);
        return suspensionEnd != null;
    }

    public static Instant getSuspensionEnd(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        var suspensionEnd = sharedPreferences.getString(SUSPENSION_END_KEY, null);
        if (suspensionEnd == null) return null;
        try {
            return Instant.parse(suspensionEnd);
        } catch (DateTimeParseException ex) {
            return null;
        }
    }

    public static <T> T extractClaim(Context context, String claimName, Class<T> type) {
        try {
            String token = getToken(context);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(BuildConfig.JWT_SECRET) // Use your backend's signing key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(claimName, type);
        } catch (Exception e) {
            if (e.getMessage() != null) Log.d("JWT", e.getMessage());
            return null;
        }
    }

    public static String getAuthorizationValue(Context context) {
        return "Bearer " + getToken(context);
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TOKEN_KEY, null);
    }
}
