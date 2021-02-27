package openfoodfacts.github.scrachx.openfood.features.changelog

import android.content.Context
import com.fasterxml.jackson.databind.ObjectMapper
import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import openfoodfacts.github.scrachx.openfood.utils.LocaleHelper.getLanguage
import java.io.BufferedReader
import java.io.IOException

class ChangelogService(private val context: Context) {
    companion object {
        private const val FOLDER = "changelog/"
    }

    private var mapper: ObjectMapper = ObjectMapper()

    fun observeChangelog(): Single<Changelog> = Single
            .fromCallable { parseJsonFile() }
            .subscribeOn(Schedulers.io())

    @Throws(IOException::class)
    private fun parseJsonFile(): Changelog {
        val language = getLanguage(context)
        val jsonString = if (translationExists("changelog-$language.json")) {
            getJsonStringFromAsset("changelog-$language.json")
        } else {
            getJsonStringFromAsset("changelog.json")
        }
        return mapper.readValue(jsonString, Changelog::class.java)
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
        val versions = context.assets.list(FOLDER)
        return versions?.toList()?.contains(fileName) ?: false

    }
}
