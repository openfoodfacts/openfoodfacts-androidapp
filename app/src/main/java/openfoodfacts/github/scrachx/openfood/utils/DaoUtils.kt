package openfoodfacts.github.scrachx.openfood.utils

import android.util.Log
import io.reactivex.Single
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy
import org.greenrobot.greendao.AbstractDao
import org.jetbrains.annotations.Contract

/**
 * Checks whether table is empty
 *
 * @param dao checks records count of any table
 */
@Contract(pure = true)
fun isDaoEmpty(dao: AbstractDao<*, *>) = dao.count() == 0L

@Contract(pure = true)
fun <T> logDownload(single: Single<List<T>?>, taxonomy: Taxonomy) = single.doOnSuccess {
    Log.i(Taxonomy::class.java.name + "getTaxonomyData", "refreshed taxonomy '$taxonomy' from server")
}