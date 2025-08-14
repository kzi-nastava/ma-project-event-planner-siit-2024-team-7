package rs.ac.uns.eventplanner.team7.data.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.data.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.auth.LoginResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.auth.RegisterRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.auth.ValidateQuickRegistrationDTO;

public interface AuthService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("auth/login")
    Call<LoginResponseDTO> login(@Body LoginRequestDTO loginRequestDTO);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("auth/register")
    Call<Void> register(@Body RegisterRequestDTO registerRequestDTO);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("quick_registrations")
    Call<Void> validateQuickRegistration(@Body ValidateQuickRegistrationDTO dto);

}
