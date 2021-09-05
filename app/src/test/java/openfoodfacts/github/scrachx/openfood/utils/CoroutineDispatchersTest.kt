package openfoodfacts.github.scrachx.openfood.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher

@ExperimentalCoroutinesApi
class CoroutineDispatchersTest : CoroutineDispatchers {
    override fun main() = TestCoroutineDispatcher()

    override fun io() = TestCoroutineDispatcher()

    override fun default() = TestCoroutineDispatcher()
}
