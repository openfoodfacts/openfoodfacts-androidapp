package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import androidx.annotation.CheckResult
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy
import org.greenrobot.greendao.AbstractDao
import org.greenrobot.greendao.query.QueryBuilder
import org.jetbrains.annotations.Contract

/**
 * Checks whether table is empty
 */
@Contract(pure = true)
@CheckResult
fun AbstractDao<*, *>.isEmpty() = count() == 0L

@Contract(pure = true)
fun <T> logDownload(taxonomy: Taxonomy<T>) {
    Log.i(
        "${Taxonomy::class.simpleName}",
        "Refreshed taxonomy '${taxonomy::class.simpleName}' from server"
    )
}

inline fun <T, R> AbstractDao<T, R>.build(builderAction: QueryBuilder<T>.() -> Unit): QueryBuilder<T> {
    val builder = queryBuilder()
    builder.builderAction()
    return builder
}

inline fun <T, R> AbstractDao<T, R>.unique(builderAction: QueryBuilder<T>.() -> Unit): T? {
    return build(builderAction).unique()
}

inline fun <T, R> AbstractDao<T, R>.list(builderAction: QueryBuilder<T>.() -> Unit = {}): List<T> {
    return build(builderAction).list()
}