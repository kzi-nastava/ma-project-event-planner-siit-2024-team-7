package rs.ac.uns.eventplanner.team7.data.services;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.CreateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.CreateServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.DeleteServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.GetServiceResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.UpdateServiceRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.service.UpdateServiceResponseDTO;

public interface ServiceService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("services/top")
    Call<List<DetailedItemDTO>> findTopFive(@Header("Authorization") @Nullable String token,
                                            @Query("city") String city);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("services/filter")
    Call<Page<BasicItemDTO>> filter(@Header("Authorization") @Nullable String token,
                                    @QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("services/all_cities")
    Call<List<String>> findAllCities();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("all")
    Call<Page<BasicItemDTO>> getAllProviderServices();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("services")
    Call<CreateServiceResponseDTO> createService(@Header("Authorization") String token, @Body CreateServiceRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("services/{id}")
    Call<UpdateServiceResponseDTO> updateService(@Header("Authorization") String token, @Path("id") Integer id, @Body UpdateServiceRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("services/{id}")
    Call<DeleteServiceResponseDTO> deleteService(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("services")
    Call<Page<BasicItemDTO>> findServicesByProvider(@Header("Authorization") String token, @QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("services/{id}")
    Call<GetServiceResponseDTO> getService(@Header("Authorization") String token, @Path("id") Integer id);
}
