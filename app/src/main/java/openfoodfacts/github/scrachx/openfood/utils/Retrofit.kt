package openfoodfacts.github.scrachx.openfood.utils

import retrofit2.Retrofit

fun Retrofit(builderAction: Retrofit.Builder.() -> Unit): Retrofit {
    return Retrofit.Builder().apply(builderAction).build()
}
