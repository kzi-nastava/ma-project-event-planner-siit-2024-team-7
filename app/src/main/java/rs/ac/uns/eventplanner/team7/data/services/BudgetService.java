package rs.ac.uns.eventplanner.team7.data.services;

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
import rs.ac.uns.eventplanner.team7.data.dto.budget.AddCategoryBudgetRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.CategoryBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.CreateBudgetRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.EventBudgetResponseDTO;
import rs.ac.uns.eventplanner.team7.data.dto.budget.UpdateCategoryBudgetRequestDTO;

public interface BudgetService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("budget")
    Call<EventBudgetResponseDTO> createBudget(@Header("Authorization") String token, @Body CreateBudgetRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("budget/{id}")
    Call<EventBudgetResponseDTO> addCategoryBudget(@Header("Authorization") String token, @Path("id") Integer id, @Body AddCategoryBudgetRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("budget/{eventBudgetId}/category_budget/{categoryBudgetId}")
    Call<CategoryBudgetResponseDTO> updateCategoryBudget(@Header("Authorization") String token,
                                                         @Path("eventBudgetId") Integer eventBudgetId,
                                                         @Path("categoryBudgetId") Integer categoryBudgetId,
                                                         @Body UpdateCategoryBudgetRequestDTO dto);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @DELETE("budget/{eventBudgetId}/category_budget/{categoryBudgetId}")
    Call<EventBudgetResponseDTO> removeCategoryBudget(@Header("Authorization") String token,
                                                      @Path("eventBudgetId") Integer eventBudgetId,
                                                      @Path("categoryBudgetId") Integer categoryBudgetId);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("budget/{id}")
    Call<EventBudgetResponseDTO> getEventBudget(@Header("Authorization") String token, @Path("id") Integer id);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("budget/{id}/category_budget")
    Call<CategoryBudgetResponseDTO> getCategoryBudget(@Header("Authorization") String token, @Path("id") Integer id, @Query("categoryName") String categoryName);
}
