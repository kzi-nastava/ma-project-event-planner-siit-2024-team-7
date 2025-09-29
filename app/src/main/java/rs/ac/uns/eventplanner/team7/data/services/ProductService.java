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
import rs.ac.uns.eventplanner.team7.data.dto.product.CreateProductRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.GetProductResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.product.UpdateProductRequestDTO;

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

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("products")
    Call<Page<BasicItemDTO>> findProductsByProvider(@Header("Authorization") String token, @QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("products")
    Call<Void> createProduct(@Header("Authorization") String token, @Body CreateProductRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("products/{id}")
    Call<Void> updateProduct(@Header("Authorization") String token, @Body UpdateProductRequestDTO dto, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("products/{id}")
    Call<Void> deleteProduct(@Header("Authorization") String token, @Path("id") Integer id);
}
