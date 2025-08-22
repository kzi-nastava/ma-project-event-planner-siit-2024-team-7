package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatContactDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.chat.ChatResponseDTO;

public interface ChatService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("chats")
    Call<ChatResponseDTO> sendChatMessage(@Header("Authorization") String token, @Body ChatRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("chats/{recipientId}")
    Call<Void> markAsRead(@Header("Authorization") String token, @Path("recipientId") Integer recipientId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("chats/contacts")
    Call<List<ChatContactDTO>> findContacts(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("chats/{recipientId}")
    Call<List<ChatResponseDTO>> findBySenderAndRecipient(@Header("Authorization") String token, @Path("recipientId") Integer recipientId);
}
