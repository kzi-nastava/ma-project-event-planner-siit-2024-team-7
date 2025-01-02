package rs.ac.uns.eventplanner.team7.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.services.AuthService;
import rs.ac.uns.eventplanner.team7.services.CategoryService;
import rs.ac.uns.eventplanner.team7.services.EventService;
import rs.ac.uns.eventplanner.team7.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.services.InvitationService;
import rs.ac.uns.eventplanner.team7.services.ProductService;
import rs.ac.uns.eventplanner.team7.services.ServiceService;
import rs.ac.uns.eventplanner.team7.services.UserService;

public final class ClientUtils {
    private static final String API_PATH = "http://" + BuildConfig.IP_ADDR +":8080/api/";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(API_PATH)
            .addConverterFactory(GsonConverterFactory.create())
            .client(test())
            .build();
    private static final Map<Class<?>, Object> serviceImplementations = new HashMap<>() {{
        put(AuthService.class, retrofit.create(AuthService.class));
        put(CategoryService.class, retrofit.create(CategoryService.class));
        put(EventService.class, retrofit.create(EventService.class));
        put(EventTypeService.class, retrofit.create(EventTypeService.class));
        put(InvitationService.class, retrofit.create(InvitationService.class));
        put(ProductService.class, retrofit.create(ProductService.class));
        put(ServiceService.class, retrofit.create(ServiceService.class));
        put(UserService.class, retrofit.create(UserService.class));
    }};

    public static <T> T injectService(Class<T> type) {
        Object implementation = serviceImplementations.get(type);
        if (implementation != null) return type.cast(implementation);
        throw new IllegalArgumentException("No implementation found for type " + type.getName());
    }

    private static OkHttpClient test(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        return new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(interceptor).build();
    }
}
