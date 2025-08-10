package rs.ac.uns.eventplanner.team7.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rs.ac.uns.eventplanner.team7.BuildConfig;
import rs.ac.uns.eventplanner.team7.data.dto.ErrorMessageDTO;
import rs.ac.uns.eventplanner.team7.data.services.AuthService;
import rs.ac.uns.eventplanner.team7.data.services.CategoryService;
import rs.ac.uns.eventplanner.team7.data.services.EventService;
import rs.ac.uns.eventplanner.team7.data.services.EventTypeService;
import rs.ac.uns.eventplanner.team7.data.services.InvitationService;
import rs.ac.uns.eventplanner.team7.data.services.NotificationService;
import rs.ac.uns.eventplanner.team7.data.services.ProductService;
import rs.ac.uns.eventplanner.team7.data.services.ReportService;
import rs.ac.uns.eventplanner.team7.data.services.ReservationService;
import rs.ac.uns.eventplanner.team7.data.services.ServiceService;
import rs.ac.uns.eventplanner.team7.data.services.UserService;

public final class ClientUtils {
    public static final String API_PATH = "http://" + BuildConfig.IP_ADDR +":8080/api/";

    private static final Map<Class<?>, Object> serviceImplementations;
    private static final Retrofit retrofit;

    static {
        final Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeConverter())
                .registerTypeAdapter(Instant.class, new InstantConverter())
                .create();

        final OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(120, TimeUnit.SECONDS)
                .readTimeout(120, TimeUnit.SECONDS)
                .writeTimeout(120, TimeUnit.SECONDS)
                .addInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(TokenInterceptor.INTERCEPTOR)
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(API_PATH)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();

        serviceImplementations = new HashMap<>() {{
            put(AuthService.class, retrofit.create(AuthService.class));
            put(CategoryService.class, retrofit.create(CategoryService.class));
            put(EventService.class, retrofit.create(EventService.class));
            put(EventTypeService.class, retrofit.create(EventTypeService.class));
            put(InvitationService.class, retrofit.create(InvitationService.class));
            put(ProductService.class, retrofit.create(ProductService.class));
            put(ServiceService.class, retrofit.create(ServiceService.class));
            put(UserService.class, retrofit.create(UserService.class));
            put(NotificationService.class, retrofit.create(NotificationService.class));
            put(ReservationService.class, retrofit.create(ReservationService.class));
            put(ReportService.class, retrofit.create(ReportService.class));
        }};
    }

    public static <T> T injectService(Class<T> type) {
        Object implementation = serviceImplementations.get(type);
        if (implementation != null) return type.cast(implementation);
        throw new IllegalArgumentException("No implementation found for type " + type.getName());
    }

    public static ErrorMessageDTO convertToErrorMessage(ResponseBody errorBody) {
        try {
            return retrofit
                    .<ErrorMessageDTO>responseBodyConverter(ErrorMessageDTO.class, new Annotation[0])
                    .convert(errorBody);
        } catch (IOException e) {
            return null;
        }
    }
}
