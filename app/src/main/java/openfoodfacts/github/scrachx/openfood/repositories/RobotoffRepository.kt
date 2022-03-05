package openfoodfacts.github.scrachx.openfood.repositories

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.Credentials
import openfoodfacts.github.scrachx.openfood.models.AnnotationAnswer
import openfoodfacts.github.scrachx.openfood.models.AnnotationResponse
import openfoodfacts.github.scrachx.openfood.models.Question
import openfoodfacts.github.scrachx.openfood.network.services.RobotoffAPI
import openfoodfacts.github.scrachx.openfood.utils.getLoginPreferences
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RobotoffRepository @Inject constructor(
    @ApplicationContext private val context: Context,
    private val robotoffAPI: RobotoffAPI
) {

    /**
     * Loads Robotoff question from the local database by code and lang of question.
     *
     * @param code for the question
     * @param lang is language of the question
     * @return The single question
     */
    suspend fun getProductQuestion(code: String, lang: String): Question? {
        val state = runCatching { robotoffAPI.getProductQuestions(code, lang, 1) }
        return state.getOrNull()?.questions?.getOrNull(0)
    }

    /**
     * Annotate the Robotoff insight response using insight id and annotation
     *
     * @return The annotated insight response
     */
    suspend fun annotateInsight(answer: AnnotationAnswer): AnnotationResponse {
        // if the user is logged in, send the auth, otherwise make it anonymous
        val user = context.getLoginPreferences().getString("user", "")?.trim { it <= ' ' } ?: ""
        val pass = context.getLoginPreferences().getString("pass", "")?.trim { it <= ' ' } ?: ""

        return if (user.isBlank() || pass.isBlank()) {
            robotoffAPI.annotateInsight(answer.insightId, answer.value.result)
        } else {
            robotoffAPI.annotateInsight(answer.insightId, answer.value.result, Credentials.basic(user, pass, Charsets.UTF_8))
        }
    }
}