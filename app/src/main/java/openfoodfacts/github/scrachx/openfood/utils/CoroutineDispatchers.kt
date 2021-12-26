package openfoodfacts.github.scrachx.openfood.utils

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

interface CoroutineDispatchers {
    val Main: CoroutineDispatcher
    val IO: CoroutineDispatcher
    val Default: CoroutineDispatcher
}

class CoroutineDispatchersDefaultImpl @Inject constructor() : CoroutineDispatchers {
    override val Main get() = Dispatchers.Main

    override val IO get() = Dispatchers.IO

    override val Default get() = Dispatchers.Default
}
