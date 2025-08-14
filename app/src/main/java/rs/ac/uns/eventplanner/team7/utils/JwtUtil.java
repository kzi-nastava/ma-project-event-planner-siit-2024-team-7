package rs.ac.uns.eventplanner.team7.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.Date;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;

public class JwtUtil {

    private static final String SECRET_KEY = "ahbsf248761FSDTG532HBaadfjHJSDYgauyasduy672762gbads67gdSDFf76DSDSD";

    private static final String PREFS_NAME = "AppPreferences";
    private static final String TOKEN_KEY = "jwt_token";
    private static final String ROLE = "role";

    private static final String CITY = "city";

    public static void saveToken(Context context, String token) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.apply();
    }

    public static void saveRole(Context context, String role) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(ROLE, role);
        editor.apply();
    }

    public static void saveCity(Context context, String city) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(CITY, city);
        editor.apply();
    }

    public static String getRole(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(ROLE, null);
    }

    public static String getToken(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(TOKEN_KEY, null);
    }

    public static String getCity(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(CITY, null);
    }

    public static void setDefaultValues(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        sharedPreferences.edit().putString(ROLE, "GUEST").putString(CITY, "All").remove(TOKEN_KEY).apply();
    }

    public static Integer extractId(Context context) {
        return extractClaim(context, "id", Integer.class);
    }

    public static Date extractExpirationDate(Context context) {
        return extractClaim(context, "exp", Date.class);
    }

    public static <T> T extractClaim(Context context, String claimName, Class<T> type) {
        try {
            String token = getToken(context);
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY) // Use your backend's signing key
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.get(claimName, type);
        } catch (ExpiredJwtException e) {
            if (e.getMessage() != null) Log.e("JWT", e.getMessage());
            return null;
        }
    }

    public static String getAuthorizationValue(Context context) {
        return "Bearer " + getToken(context);
    }
}
