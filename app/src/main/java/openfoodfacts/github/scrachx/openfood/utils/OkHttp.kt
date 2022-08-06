package openfoodfacts.github.scrachx.openfood.utils

import okhttp3.ConnectionSpec
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor

inline fun OkHttpClient(builderAction: OkHttpClient.Builder.() -> Unit): OkHttpClient {
    return OkHttpClient.Builder().apply(builderAction).build()
}

inline fun OkHttpClient.buildUpon(builderAction: OkHttpClient.Builder.() -> Unit): OkHttpClient {
    return newBuilder().apply(builderAction).build()
}

inline fun Request.buildUpon(builderAction: Request.Builder.() -> Unit): Request {
    return newBuilder().apply(builderAction).build()
}

inline fun ConnectionSpec(
    connectionSpec: ConnectionSpec,
    builderAction: ConnectionSpec.Builder.() -> Unit,
): ConnectionSpec {
    return ConnectionSpec.Builder(connectionSpec)
        .apply(builderAction)
        .build()
}

inline fun HttpLoggingInterceptor(builderAction: HttpLoggingInterceptor.() -> Unit): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply(builderAction)
}