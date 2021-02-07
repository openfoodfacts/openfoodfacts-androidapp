package openfoodfacts.github.scrachx.openfood.network.services

import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse
import openfoodfacts.github.scrachx.openfood.models.QuestionsState
import retrofit2.http.*

interface RobotoffAPI {
    companion object {
        const val API_P = "api/v1"
    }

    @GET("$API_P/questions/{barcode}")
    fun getProductQuestions(
            @Path("barcode") barcode: String,
            @Query("lang") langCode: String,
            @Query("count") count: Int
    ): Single<QuestionsState>

    @FormUrlEncoded
    @POST("$API_P/insights/annotate")
    fun annotateInsight(
            @Field("insight_id") insightId: String,
            @Field("annotation") annotation: Int
    ): Single<AnnotationResponse>

    @FormUrlEncoded
    @POST("$API_P/insights/annotate")
    fun annotateInsight(
            @Field("insight_id") insightId: String,
            @Field("annotation") annotation: Int,
            @Header("Authorization") auth: String
    ): Single<AnnotationResponse>
}