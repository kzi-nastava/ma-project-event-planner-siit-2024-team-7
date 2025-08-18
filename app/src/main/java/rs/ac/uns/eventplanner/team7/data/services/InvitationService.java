package rs.ac.uns.eventplanner.team7.data.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.data.dto.ResponseMessageDTO;
import rs.ac.uns.eventplanner.team7.data.dto.invitation.InvitationAcceptanceDTO;
import rs.ac.uns.eventplanner.team7.data.dto.invitation.InvitationSendingDTO;

public interface InvitationService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("invitations/send")
    Call<ResponseMessageDTO> sendInvitations(@Header("Authorization") String token, @Body InvitationSendingDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("invitations/accept_from_app")
    Call<ResponseMessageDTO> acceptInvitation(@Header("Authorization") String token, @Body InvitationAcceptanceDTO dto);
}
