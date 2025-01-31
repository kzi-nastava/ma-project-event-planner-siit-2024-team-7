package rs.ac.uns.eventplanner.team7.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.dto.category.CategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.CreateCategoryRequestDTO;

public interface CategoryService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories")
    Call<List<CategoryResponseDTO>> getAll(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories/all_names")
    Call<List<String>> findAllNames();

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("categories")
    Call<CategoryResponseDTO> createCategory(@Header("Authorization") String token, @Body CreateCategoryRequestDTO dto);
}

