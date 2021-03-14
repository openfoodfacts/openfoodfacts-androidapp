package openfoodfacts.github.scrachx.openfood.features

import androidx.annotation.CallSuper
import androidx.fragment.app.Fragment
import io.reactivex.disposables.CompositeDisposable
import openfoodfacts.github.scrachx.openfood.features.viewmodel.BaseViewModel

abstract class MVVMFragment<T : BaseViewModel> : Fragment() {
    private val compositeDisposable = CompositeDisposable()

    @CallSuper
    override fun onStart() {
        super.onStart()
        viewModel.bind()
        bindProperties(compositeDisposable)
    }

    @CallSuper
    override fun onStop() {
        super.onStop()
        compositeDisposable.clear()
        viewModel.unbind()
    }


    protected abstract val viewModel: T
    protected abstract fun bindProperties(compositeDisposable: CompositeDisposable?)
}