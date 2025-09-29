package rs.ac.uns.eventplanner.team7.utils;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.IOException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class TokenInterceptor implements Interceptor {
    public interface OnTokenExpiredListener {
        void onTokenExpired();
    }

    public static final TokenInterceptor INTERCEPTOR = new TokenInterceptor();

    private static Date tokenExpirationDate;
    private static OnTokenExpiredListener listener;
    private static final AtomicBoolean isTriggered = new AtomicBoolean(false);

    private TokenInterceptor() {}

    public static void register(Date tokenExpirationDate, OnTokenExpiredListener listener) {
        TokenInterceptor.tokenExpirationDate = tokenExpirationDate;
        TokenInterceptor.listener = listener;
        Log.d("TokenInterceptor", "Registered interceptor");
    }

    public static void unregister() {
        if (TokenInterceptor.tokenExpirationDate == null || TokenInterceptor.listener == null) return;
        TokenInterceptor.tokenExpirationDate = null;
        TokenInterceptor.listener = null;
        isTriggered.set(false);
        Log.d("TokenInterceptor", "Unregistered interceptor");
    }

    @NonNull
    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        boolean tokenExpired = tokenExpirationDate != null && isTokenExpired();

        if (listener != null && tokenExpired) {
            if (isTriggered.compareAndSet(false, true)) {
                Log.d("TokenInterceptor", "Token expired");

                new Handler(Looper.getMainLooper()).post(() -> listener.onTokenExpired());

                throw new IOException("Session expired"); // Prevent further request execution
            }
        }

        return chain.proceed(request);
    }

    private boolean isTokenExpired() {
        return tokenExpirationDate.before(new Date());
    }
}