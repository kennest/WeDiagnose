package wesicknessdect.example.org.wesicknessdetect.retrofit;


import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;
import wesicknessdect.example.org.wesicknessdetect.models.Country;
import wesicknessdect.example.org.wesicknessdetect.models.Credential;
import wesicknessdect.example.org.wesicknessdetect.models.Culture;
import wesicknessdect.example.org.wesicknessdetect.models.CulturePart;
import wesicknessdect.example.org.wesicknessdetect.models.Disease;
import wesicknessdect.example.org.wesicknessdetect.models.Model;
import wesicknessdect.example.org.wesicknessdetect.models.Question;
import wesicknessdect.example.org.wesicknessdetect.models.Struggle;
import wesicknessdect.example.org.wesicknessdetect.models.StruggleResponse;
import wesicknessdect.example.org.wesicknessdetect.models.Symptom;
import wesicknessdect.example.org.wesicknessdetect.models.User;

public interface APIService {
    @GET
    Call<ResponseBody> downloadModelWithDynamicUrlSync(@Url String fileUrl);

    @Headers({"Content-Type: application/json","Accept:application/json"})
    @POST("login/")
    Call<User> doLogin(@Body Credential credential);

    @Headers({"Content-Type:application/json","Accept:application/json"})
    @POST("api/users/")
    Call<User> doSignup(@Body User user);

    @GET("api/country/")
    Call<List<Country>> getCountries();

    @GET("api/cultures/")
    Call<List<Culture>> getCultures();

    @GET("api/partcultures/")
    Call<List<CulturePart>> getCulturePart(@Query("culture") int id);

    @GET("api/models/")
    Call<List<Model>> getModel(@Query("part") int part_id);

    @GET("api/models")
    Call<Model> getModels(@Header("Authorization") String token);

    @GET("api/questions")
    Call<List<Question>> getQuestion();

    @GET("api/symptoms")
    Call<List<Symptom>> getSymptoms();

    @GET("api/diseases")
    Call<List<Disease>> getDiseases();

    @GET("api/struggles")
    Call<StruggleResponse> getStruggles();

}
