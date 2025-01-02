package rs.ac.uns.eventplanner.team7.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.CreateEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.UpdateEventTypeResponseDTO;

public interface EventTypeService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("event_types")
    Call<List<GetEventTypeResponseDTO>> getAll(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("event_types")
    Call<CreateEventTypeResponseDTO> create(@Header("Authorization") String token, @Body CreateEventTypeRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("event_types/{id}")
    Call<GetEventTypeResponseDTO> get(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("event_types/{id}")
    Call<UpdateEventTypeResponseDTO> update(@Header("Authorization") String token, @Path("id") Integer id, @Body UpdateEventTypeRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("event_types/{id}")
    Call<Void> delete(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("event_types/all_names")
    Call<List<String>> findAllNames();
}
