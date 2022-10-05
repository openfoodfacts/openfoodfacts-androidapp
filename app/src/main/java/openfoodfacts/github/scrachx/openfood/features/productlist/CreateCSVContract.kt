package openfoodfacts.github.scrachx.openfood.features.productlist

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.KITKAT)
class CreateCSVContract : ActivityResultContracts.CreateDocument() {
    override fun createIntent(context: Context, input: String) =
        super.createIntent(context, input)
            .setType("application/csv")
            .addCategory(Intent.CATEGORY_OPENABLE)

}