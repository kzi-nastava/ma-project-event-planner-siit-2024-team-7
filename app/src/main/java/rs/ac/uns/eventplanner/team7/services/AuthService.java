package rs.ac.uns.eventplanner.team7.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.auth.LoginResponseDTO;

public interface AuthService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("/auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO loginRequestDTO);

}
