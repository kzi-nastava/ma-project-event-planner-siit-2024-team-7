package rs.ac.uns.eventplanner.team7.data.services;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;

public interface ProductService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/top")
    Call<List<DetailedItemDTO>> findTopFive(@Header("Authorization") @Nullable String token,
                                            @Query("city") String city);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/filter")
    Call<Page<BasicItemDTO>> filter(@Header("Authorization") @Nullable String token,
                                    @QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/all_cities")
    Call<List<String>> findAllCities();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/{id}")
    Call<GetProductResponseDTO> getProduct(@Header("Authorization") String token, @Path("id") Integer id);
}
