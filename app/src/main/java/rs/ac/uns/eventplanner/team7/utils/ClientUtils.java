package rs.ac.uns.eventplanner.team7.utils;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rs.ac.uns.eventplanner.team7.BuildConfig;

public class ClientUtils {

    private static final String API_PATH = "http://" + BuildConfig.IP_ADDR +":8080/api/";

    public static OkHttpClient test(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();
    }


    public static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_PATH)
            .addConverterFactory(GsonConverterFactory.create())
            .client(test())
            .build();
}
