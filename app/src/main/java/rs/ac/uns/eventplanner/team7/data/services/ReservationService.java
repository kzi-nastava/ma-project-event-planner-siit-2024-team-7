package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.AvailableTimeSlotsDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.CreateReservationRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reservation.ReservationDTO;


public interface ReservationService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("reservations")
    Call<ReservationDTO> create(
            @Header("Authorization") String token,
            @Body CreateReservationRequestDTO dto
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("reservations/events")
    Call<List<ReservationDTO>> getAllForEvent(
            @Header("Authorization") String token,
            @Query("eventId") int eventId
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("reservations/services")
    Call<List<ReservationDTO>> getAllForService(
            @Header("Authorization") String token,
            @Query("serviceId") int serviceId
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("reservations/services/{serviceId}/availability")
    Call<AvailableTimeSlotsDTO> getAvailableTimeSlotsForDate(
            @Header("Authorization") String token,
            @Path("serviceId") int serviceId,
            @Query("date") String date  // format: yyyy-MM-dd
    );

}
