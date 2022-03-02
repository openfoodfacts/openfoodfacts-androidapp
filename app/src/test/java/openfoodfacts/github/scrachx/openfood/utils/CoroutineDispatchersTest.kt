package openfoodfacts.github.scrachx.openfood.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class CoroutineDispatchersTest : CoroutineDispatchers {
    override val Main get() = TestCoroutineDispatcher()

    override val IO get() = TestCoroutineDispatcher()

    override val Default get() = TestCoroutineDispatcher()
}
