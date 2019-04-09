package openfoodfacts.github.scrachx.openfood.network;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.models.InsightAnnotationResponse;
import openfoodfacts.github.scrachx.openfood.models.QuestionsState;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface RobotoffAPIService {
    @GET("api/v1/questions/{barcode}")
    Single<QuestionsState> getProductQuestion(@Path("barcode") String barcode,
                                              @Query("lang") String langCode,
                                              @Query("count") Integer count);

    @FormUrlEncoded
    @POST("api/v1/insights/annotate")
    Single<InsightAnnotationResponse> annotateInsight(@Field("insight_id") String insightId, @Field("annotation") int annotation);
}
