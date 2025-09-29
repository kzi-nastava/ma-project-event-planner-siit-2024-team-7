package rs.ac.uns.eventplanner.team7.data.services;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import rs.ac.uns.eventplanner.team7.data.dto.purchase.ProductPurchaseRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.purchase.ProductPurchaseResponseDTO;

public interface PurchaseService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("purchase")
    Call<ProductPurchaseResponseDTO> create(@Header("Authorization") String token, @Body ProductPurchaseRequestDTO dto);
}
