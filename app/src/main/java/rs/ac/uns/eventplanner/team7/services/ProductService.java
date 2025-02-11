package rs.ac.uns.eventplanner.team7.services;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.dto.Page;
import rs.ac.uns.eventplanner.team7.dto.item.BasicItemDTO;
import rs.ac.uns.eventplanner.team7.dto.item.DetailedItemDTO;
import rs.ac.uns.eventplanner.team7.dto.product.GetProductResponseDTO;

public interface ProductService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/top")
    Call<List<DetailedItemDTO>> findTopFive(@Query("city") String city);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products/filter")
    Call<Page<BasicItemDTO>> filter(@QueryMap Map<String, String> filters);

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
