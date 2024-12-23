package rs.ac.uns.eventplanner.team7.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.event_type.GetEventTypeResponseDTO;

public interface EventTypeService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("event_types")
    Call<List<GetEventTypeResponseDTO>> getAll();
}
