package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import rs.ac.uns.eventplanner.team7.data.dto.pricing.PricingResponseDTO;


public interface PricingService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("items/pricing/all")
    Call<List<PricingResponseDTO>> getAllPricing(@Header("Authorization") String token);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("items/pricing/pdf")
    Call<ResponseBody> exportPriceListPdf(@Header("Authorization") String token);
}
