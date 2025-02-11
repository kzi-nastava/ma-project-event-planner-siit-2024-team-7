package rs.ac.uns.eventplanner.team7.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.dto.invitation.InvitationAcceptanceDTO;

public interface InvitationService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("invitations/send")
    Call<ResponseMessageDTO> sendInvitations(@Header("Authorization") String token, @Body InvitationAcceptanceDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("invitations/accept_from_app")
    Call<ResponseMessageDTO> acceptInvitation(@Header("Authorization") String token, @Body InvitationAcceptanceDTO dto);
}
