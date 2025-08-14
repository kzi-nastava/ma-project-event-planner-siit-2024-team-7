package rs.ac.uns.eventplanner.team7.data.services;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.PUT;
import retrofit2.http.Headers;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ImagesService {

    @Headers({
            "User-Agent: Mobile-Android",
    })
    @Multipart
    @PUT("images/service/{id}")
    Call<List<String>> uploadImagesForService(@Header("Authorization") String token,
                                                       @Path("id") Integer id,
                                                       @Part List<MultipartBody.Part> images,
                                                       @Query("imageUrls") List<String> imageUrls);
}
