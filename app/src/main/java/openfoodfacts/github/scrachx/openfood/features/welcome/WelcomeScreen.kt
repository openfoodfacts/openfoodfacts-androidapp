package openfoodfacts.github.scrachx.openfood.features.welcome

import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import openfoodfacts.github.scrachx.openfood.R

internal enum class WelcomeScreen(@StringRes layoutRes: Int, @ColorRes colorRes: Int) {
    INTRO(R.layout.welcome_slide_intro, R.color.bg_welcome_intro),
    SCREEN_1(R.layout.welcome_slide_1, R.color.bg_welcome_nutriscore),
    SCREEN_2(R.layout.welcome_slide_2, R.color.bg_welcome_nova),
    SCREEN_3(R.layout.welcome_slide_3, R.color.bg_welcome_ecoscore),
    MATOMO(R.layout.welcome_slide_matomo, R.color.bg_welcome_matomo);

    @StringRes
    val layout = layoutRes

    @ColorRes
    val color = colorRes

    companion object {
        operator fun get(position: Int) = values()[position]
    }
}