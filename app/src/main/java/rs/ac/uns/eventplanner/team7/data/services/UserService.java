package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import rs.ac.uns.eventplanner.team7.data.dto.BusynessDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.FavouriteItemResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.GetProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UpdateOrganizerRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UpdateOrganizerResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UpdateProviderRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UpdateProviderResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.user.UserPreferencesDTO;

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

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("users/organizers/{id}")
    Call<UpdateOrganizerResponseDTO> updateOrganizer(@Header("Authorization") String token, @Path("id") Integer id,
                                                     @Body UpdateOrganizerRequestDTO dto);


    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("users/providers/{id}")
    Call<UpdateProviderResponseDTO> updateProvider(@Header("Authorization") String token, @Path("id") Integer id,
                                                   @Body UpdateProviderRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("users/{id}/busyness")
    Call<List<BusynessDTO>> getBusyness(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("users/organizers/{id}")
    Call<Object> deactivateOrganizer(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("users/providers/{id}")
    Call<Object> deactivateProvider(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("users/{id}/favourites/items")
    Call<FavouriteItemResponseDTO> markItemAsFavourite(@Header("Authorization") String token, @Path("id") Integer id, @Body FavouriteItemRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("users/providers/by_item/{itemId}")
    Call<GetProviderResponseDTO> getProviderByItemId(@Header("Authorization") String token, @Path("itemId") Integer itemId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("users/{id}/preferences")
    Call<UserPreferencesDTO> getPreferences(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("users/{id}/preferences")
    Call<UserPreferencesDTO> updatePreferences(@Header("Authorization") String token, @Path("id") Integer id, @Body UserPreferencesDTO preferences);
}
