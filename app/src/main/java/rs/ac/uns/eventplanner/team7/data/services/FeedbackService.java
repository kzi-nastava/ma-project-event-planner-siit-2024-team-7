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
import retrofit2.http.Query;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.AverageRatingDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateEventFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateItemFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.CreateProviderFeedbackRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.FeedbackDTO;
import rs.ac.uns.eventplanner.team7.data.dto.feedback.UpdateFeedbackRequestDTO;

public interface FeedbackService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("feedback/items")
    Call<FeedbackDTO> createForItem(@Header("Authorization") String token,
                                    @Body CreateItemFeedbackRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("feedback/events")
    Call<FeedbackDTO> createForEvent(@Header("Authorization") String token,
                                     @Body CreateEventFeedbackRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("feedback/providers")
    Call<FeedbackDTO> createForProvider(@Header("Authorization") String token,
                                        @Body CreateProviderFeedbackRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("feedback/{feedbackId}")
    Call<FeedbackDTO> update(@Header("Authorization") String token,
                             @Path("feedbackId") Integer feedbackId,
                             @Body UpdateFeedbackRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/pending")
    Call<Page<FeedbackDTO>> getAllPending(@Header("Authorization") String token,
                                          @Query("page") int page);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/approved/items")
    Call<List<FeedbackDTO>> getAllApprovedForItem(@Header("Authorization") String token, @Query("itemId") Integer itemId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/approved/events")
    Call<List<FeedbackDTO>> getAllApprovedForEvent(@Header("Authorization") String token, @Query("eventId") Integer eventId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/approved/providers")
    Call<List<FeedbackDTO>> getAllApprovedForProvider(@Header("Authorization") String token, @Query("providerId") Integer providerId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/average/items")
    Call<AverageRatingDTO> getAverageRatingForItem(@Header("Authorization") String token, @Query("itemId") Integer itemId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/average/events")
    Call<AverageRatingDTO> getAverageRatingForEvent(@Header("Authorization") String token, @Query("eventId") Integer eventId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("feedback/average/providers")
    Call<AverageRatingDTO> getAverageRatingForProvider(@Header("Authorization") String token, @Query("providerId") Integer providerId);
}