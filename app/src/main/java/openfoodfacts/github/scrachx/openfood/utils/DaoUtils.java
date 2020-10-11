package openfoodfacts.github.scrachx.openfood.utils;

import android.util.Log;

import androidx.annotation.NonNull;

import org.greenrobot.greendao.AbstractDao;
import org.jetbrains.annotations.Contract;

import java.util.List;

import io.reactivex.Single;
import openfoodfacts.github.scrachx.openfood.repositories.Taxonomy;

public class DaoUtils {
    private DaoUtils() {
        // Utility class
    }

    @Contract(pure = true)
    public static <T> Single<List<T>> logDownload(@NonNull Single<List<T>> single, Taxonomy taxonomy) {
        return single.doOnSuccess(ts -> Log.i(Taxonomy.class.getName() + "getTaxonomyData", "refreshed taxonomy '" + taxonomy + "' from server"));
    }

    /**
     * Checks whether table is empty
     *
     * @param dao checks records count of any table
     */
    @Contract(pure = true)
    public static boolean isDaoEmpty(@NonNull AbstractDao<?, ?> dao) {
        return dao.count() == 0;
    }
}
