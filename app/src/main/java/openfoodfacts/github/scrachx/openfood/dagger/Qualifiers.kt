package openfoodfacts.github.scrachx.openfood.dagger

import javax.inject.Qualifier

class Qualifiers {
    @Qualifier
    annotation class ForApplication

    @Qualifier
    annotation class ForActivity
}