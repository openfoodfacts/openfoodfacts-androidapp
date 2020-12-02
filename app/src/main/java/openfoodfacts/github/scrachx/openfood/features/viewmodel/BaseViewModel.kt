package openfoodfacts.github.scrachx.openfood.features.viewmodel

import io.reactivex.disposables.CompositeDisposable

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
abstract class BaseViewModel {
    var subscriptions: CompositeDisposable? = null
    fun bind() {
        unbind()
        subscriptions = CompositeDisposable()
        subscribe(subscriptions!!)
    }

    fun unbind() {
        subscriptions?.let {
            it.clear()
            subscriptions = null
        }
    }

    protected abstract fun subscribe(subscriptions: CompositeDisposable)
}