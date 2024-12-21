package rs.ac.uns.eventplanner.team7.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.user.GetProviderResponseDTO;

public interface UserService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("users/organizers/{id}")
    Call<GetOrganizerResponseDTO> getOrganizer(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("users/providers/{id}")
    Call<GetProviderResponseDTO> getProvider(@Header("Authorization") String token, @Path("id") Integer id);


}
