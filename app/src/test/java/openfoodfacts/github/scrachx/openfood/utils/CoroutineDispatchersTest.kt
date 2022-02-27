package openfoodfacts.github.scrachx.openfood.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher

@ExperimentalCoroutinesApi
class CoroutineDispatchersTest : CoroutineDispatchers {
    override val Main get() = UnconfinedTestDispatcher()

    override val IO get() = UnconfinedTestDispatcher()

    override val Default get() = UnconfinedTestDispatcher()
}
