package rs.ac.uns.eventplanner.team7.data.services;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.notification.PersonalNotificationDTO;

public interface NotificationService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("notifications/{personalId}")
    Call<Void> markAsRead(@Header("Authorization") String token, @Path("personalId") Integer personalId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("notifications")
    Call<Void> markAllAsRead(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("notifications/{personalId}")
    Call<Void> deleteOne(@Header("Authorization") String token, @Path("personalId") Integer personalId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("notifications")
    Call<Void> deleteAll(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("notifications")
    Call<Page<PersonalNotificationDTO>> getNotifications(@Header("Authorization") String token,
                                                         @QueryMap Map<String, String> pageableParams);
}
