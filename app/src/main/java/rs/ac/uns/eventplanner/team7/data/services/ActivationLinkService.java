package rs.ac.uns.eventplanner.team7.data.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rs.ac.uns.eventplanner.team7.data.dto.auth.LoginRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.auth.LoginResponseDTO;

public interface ActivationLinkService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("activation-links/{id}")
    Call<Void> activate(@Header("Authorization") String token, @Path("id") Integer id);
}
