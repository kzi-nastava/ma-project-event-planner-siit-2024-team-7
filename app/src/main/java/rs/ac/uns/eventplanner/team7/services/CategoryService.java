package rs.ac.uns.eventplanner.team7.services;

import java.util.List;

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
import rs.ac.uns.eventplanner.team7.model.Category;
import rs.ac.uns.eventplanner.team7.dto.category.CreateCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.category.DeleteCategoryResponseDTO;
import rs.ac.uns.eventplanner.team7.dto.category.RejectCategoryRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.category.UpdateCategoryRequestDTO;

public interface CategoryService {
    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories")
    Call<List<Category>> getAll(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories/all_active")
    Call<List<Category>> findAllActive(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories/all_pending")
    Call<List<Category>> findAllPending(@Header("Authorization") String token);

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
    Call<Category> createCategory(@Header("Authorization") String token, @Body CreateCategoryRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("categories/{id}")
    Call<Category> updateCategory(@Header("Authorization") String token, @Body UpdateCategoryRequestDTO dto, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("categories/{id}")
    Call<DeleteCategoryResponseDTO> deleteCategory(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("categories/reject/{id}")
    Call<DeleteCategoryResponseDTO> rejectCategory(@Header("Authorization") String token, @Path("id") Integer id, @Body RejectCategoryRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories/filter/active")
    Call<List<Category>> filterActiveCategoriesByName(@Header("Authorization") String token, @Query("name") String query);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("categories/filter/pending")
    Call<List<Category>> filterPendingCategoriesByName(@Header("Authorization") String token, @Query("name") String query);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("categories/recommended/{id}")
    Call<Category> acceptRecommendedCategory(@Header("Authorization") String token, @Path("id") Integer id);

}

