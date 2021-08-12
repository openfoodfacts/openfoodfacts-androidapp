package openfoodfacts.github.scrachx.openfood.features.product.edit.overview

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import openfoodfacts.github.scrachx.openfood.R
import openfoodfacts.github.scrachx.openfood.utils.LocaleUtils
import openfoodfacts.github.scrachx.openfood.utils.SupportedLanguages

object ProductLanguagePicker {
    fun showPicker(context: Context, currentLanguageCode: String?, onItemSelected: (String) -> Unit) {
        val languageCodes = SupportedLanguages.codes()
        val selectedIndex = languageCodes.indexOf(currentLanguageCode)
        val languageItems = languageCodes.map { languageCode ->
            val locale = LocaleUtils.parseLocale(languageCode)
            val languageName = locale.getDisplayName(locale).replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
            "$languageName [$languageCode]"
        }
        MaterialAlertDialogBuilder(context)
            .setTitle(R.string.preference_choose_language_dialog_title)
            .setSingleChoiceItems(languageItems.toTypedArray(), selectedIndex) { dialog, which ->
                dialog.dismiss()
                onItemSelected(languageCodes[which])
            }
            .show()
    }
}
