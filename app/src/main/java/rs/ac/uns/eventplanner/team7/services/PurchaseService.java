package rs.ac.uns.eventplanner.team7.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.dto.purchase.ProductPurchaseRequestDTO;
import rs.ac.uns.eventplanner.team7.dto.purchase.ProductPurchaseResponseDTO;

public interface PurchaseService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("purchase")
    Call<ProductPurchaseResponseDTO> create(@Header("Authorization") String token, @Body ProductPurchaseRequestDTO dto);
}
