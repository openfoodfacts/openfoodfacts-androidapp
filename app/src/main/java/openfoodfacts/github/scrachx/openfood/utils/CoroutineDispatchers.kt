package openfoodfacts.github.scrachx.openfood.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface CoroutineDispatchers {

    fun main(): CoroutineDispatcher
    fun io(): CoroutineDispatcher
    fun default(): CoroutineDispatcher
}

class CoroutineDispatchersImpl @Inject constructor() : CoroutineDispatchers {
    override fun main() = Dispatchers.Main

    override fun io() = Dispatchers.IO

    override fun default() = Dispatchers.Default

}
