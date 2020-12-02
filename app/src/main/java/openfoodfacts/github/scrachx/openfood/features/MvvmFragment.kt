package openfoodfacts.github.scrachx.openfood.features

import android.os.Bundle
import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.features.viewmodel.BaseViewModel

abstract class MvvmFragment<T :  BaseViewModel, U> : Fragment() {
    protected val component: U? by lazy {createComponent()}
    private val compositeDisposable = CompositeDisposable()
    @CallSuper
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        inject()
    }

    @CallSuper
    override fun onStart() {
        super.onStart()
        bindViewModel()
        bindProperties(compositeDisposable)
    }

    private fun bindViewModel() {
        viewModel.bind()
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.unbind()
    }


    protected abstract val viewModel: T
    protected abstract fun createComponent(): U
    protected abstract fun inject()
    protected abstract fun bindProperties(compositeDisposable: CompositeDisposable?)
}