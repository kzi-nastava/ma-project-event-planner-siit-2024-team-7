package rs.ac.uns.eventplanner.team7.services;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;
import retrofit2.http.QueryMap;
import rs.ac.uns.eventplanner.team7.dto.Page;
import rs.ac.uns.eventplanner.team7.dto.event.BasicEventDTO;
import rs.ac.uns.eventplanner.team7.dto.event.DetailedEventDTO;

public interface EventService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/top")
    Call<List<DetailedEventDTO>> findTopFive(@Query("city") String city);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/filter")
    Call<Page<BasicEventDTO>> filter(@QueryMap Map<String, String> filters);

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("events/all_cities")
    Call<List<String>> findAllCities();
}
