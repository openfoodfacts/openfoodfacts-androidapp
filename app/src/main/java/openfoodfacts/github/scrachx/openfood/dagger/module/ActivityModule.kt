package openfoodfacts.github.scrachx.openfood.dagger.module

import androidx.appcompat.app.AppCompatActivity
import dagger.Module
import dagger.Provides
import openfoodfacts.github.scrachx.openfood.dagger.ActivityScope
import openfoodfacts.github.scrachx.openfood.dagger.Qualifiers.ForActivity

@Module
class ActivityModule(private val activity: AppCompatActivity) {
    @Provides
    @ForActivity
    @ActivityScope
    fun provideActivityContext() = activity
}