package rs.ac.uns.eventplanner.team7.data.services;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.CreateEventRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.CreateEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.DetailedEventDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.EventStatistics;
import rs.ac.uns.eventplanner.team7.data.dto.event.FutureReservableEventsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.GetEventResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.event.UpdateEventRequestDTO;

public interface EventService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/top")
    Call<List<DetailedEventDTO>> findTopFive(@Header("Authorization") @Nullable String token,
                                             @Query("city") String city);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/filter")
    Call<Page<BasicEventDTO>> filter(@Header("Authorization") @Nullable String token,
                                     @QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/all_cities")
    Call<List<String>> findAllCities();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/can_reserve_service")
    Call<FutureReservableEventsDTO> getOrganizerFutureReservableEvents(
            @Header("Authorization") String token,
            @Query("serviceId") Integer serviceId,
            @Query("organizerEmail") String organizerEmail
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events")
    Call<Page<BasicEventDTO>> getOrganizerEvents(@Header("Authorization") String token, @Query("organizerId") Integer organizerId, @Query("name") String name);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/{id}")
    Call<GetEventResponseDTO> getEvent(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("events")
    Call<CreateEventResponseDTO> createEvent(@Header("Authorization") String token, @Body CreateEventRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("events/{id}")
    Call<CreateEventResponseDTO> updateEvent(@Header("Authorization") String token, @Path("id") Integer id, @Body UpdateEventRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("events/{id}")
    Call<Void> deleteEvent(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/{id}/statistics")
    Call<EventStatistics> getEventStatistics(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
    })
    @GET("events/{id}/pdf/details")
    Call<ResponseBody> getEventDetailsPdf(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
    })
    @GET("events/{id}/pdf/guest-list")
    Call<ResponseBody> getEventGuestListPdf(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
    })
    @GET("events/{id}/pdf/statistics")
    Call<ResponseBody> getEventStatisticsPdf(@Header("Authorization") String token, @Path("id") Integer id);
}
