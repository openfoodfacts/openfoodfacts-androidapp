package openfoodfacts.github.scrachx.openfood.hilt

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.DefaultDispatcher
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.IODispatcher
import openfoodfacts.github.scrachx.openfood.hilt.qualifiers.MainDispatcher
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchers
import openfoodfacts.github.scrachx.openfood.utils.CoroutineDispatchersDefaultImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoroutineModule {

    @Binds
    @Singleton
    abstract fun bindCoroutineDispatchers(impl: CoroutineDispatchersDefaultImpl): CoroutineDispatchers

    @Binds
    @Singleton
    @IODispatcher
    fun bindIODispatcher(impl: CoroutineDispatchersDefaultImpl): CoroutineDispatcher = impl.IO

    @Binds
    @Singleton
    @MainDispatcher
    fun bindMainDispatcher(impl: CoroutineDispatchersDefaultImpl): CoroutineDispatcher = impl.Main

    @Binds
    @Singleton
    @DefaultDispatcher
    fun bindDefaultDispatcher(impl: CoroutineDispatchersDefaultImpl): CoroutineDispatcher = impl.Default

}
