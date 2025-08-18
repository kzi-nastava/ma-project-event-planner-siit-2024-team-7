package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rs.ac.uns.eventplanner.team7.data.dto.Page;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.CreateReportRequestDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.ReportDTO;
import rs.ac.uns.eventplanner.team7.data.dto.reporting.UpdateReportRequestDTO;

public interface ReportService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @POST("reports")
    Call<ReportDTO> create(
            @Header("Authorization") String token,
            @Body CreateReportRequestDTO dto
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @PUT("reports/{reportId}")
    Call<List<ReportDTO>> update(
            @Header("Authorization") String token,
            @Path("reportId") int reportId,
            @Body UpdateReportRequestDTO dto
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("reports")
    Call<Page<ReportDTO>> getAllReports(
            @Header("Authorization") String token,
            @Query("page") int page
    );

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type: application/json"
    })
    @GET("reports/undecided")
    Call<Page<ReportDTO>> getAllUndecidedReports(
            @Header("Authorization") String token,
            @Query("page") int page
    );

}
