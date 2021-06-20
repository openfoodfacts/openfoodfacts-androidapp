package openfoodfacts.github.scrachx.openfood.features.changelog

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dagger.hilt.android.qualifiers.ApplicationContext
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.models.Changelog
import openfoodfacts.github.scrachx.openfood.utils.LocaleManager
import java.io.BufferedReader
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChangelogService @Inject constructor(
    @ApplicationContext private val context: Context,
    private val localeManager: LocaleManager
) {

    companion object {
        private const val FOLDER = "changelog/"
    }

    private val mapper = ObjectMapper()

    fun observeChangelog() = Single
        .fromCallable { parseJsonFile() }
        .subscribeOn(Schedulers.io())

    @Throws(IOException::class)
    private fun parseJsonFile(): Changelog {
        val language = localeManager.getLanguage()
        val jsonString = if (translationExists("changelog-$language.json")) {
            getJsonStringFromAsset("changelog-$language.json")
        } else {
            getJsonStringFromAsset("changelog.json")
        }
        return mapper.readValue(jsonString)
    }

    @Throws(IOException::class)
    private fun getJsonStringFromAsset(fileName: String): String {
        return context.assets
                .open(FOLDER + fileName)
                .bufferedReader()
                .use(BufferedReader::readText)
    }

    @Throws(IOException::class)
    private fun translationExists(fileName: String): Boolean {
        val files = context.assets.list(FOLDER)
        return files?.contains(fileName) ?: false
    }
}
