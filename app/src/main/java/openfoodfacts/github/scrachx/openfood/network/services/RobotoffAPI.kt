package openfoodfacts.github.scrachx.openfood.network.services

import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse
import openfoodfacts.github.scrachx.openfood.models.QuestionsState
import retrofit2.http.*

interface RobotoffAPI {
    @GET("api/v1/questions/{barcode}")
    fun getProductQuestions(
            @Path("barcode") barcode: String?,
            @Query("lang") langCode: String?,
            @Query("count") count: Int?
    ): Single<QuestionsState>

    @FormUrlEncoded
    @POST("api/v1/insights/annotate")
    fun annotateInsight(
            @Field("insight_id") insightId: String?,
            @Field("annotation") annotation: Int
    ): Single<AnnotationResponse>

    @FormUrlEncoded
    @POST("api/v1/insights/annotate")
    fun annotateInsight(
            @Field("insight_id") insightId: String?,
            @Field("annotation") annotation: Int,
            @Header("Authorization") auth: String?
    ): Single<AnnotationResponse>
}