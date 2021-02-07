package openfoodfacts.github.scrachx.openfood.views.viewmodel;

import androidx.annotation.NonNull;

import io.reactivex.disposables.CompositeDisposable;

/**
 * Created by Abdelali Eramli on 27/12/2017.
 */
public abstract class BaseViewModel {
    protected CompositeDisposable subscriptions;

    public BaseViewModel() {
    }

    public void bind() {
        unbind();
        subscriptions = new CompositeDisposable();
        subscribe(subscriptions);
    }

    public void unbind() {
        if (subscriptions != null) {
            subscriptions.clear();
            subscriptions = null;
        }
    }

    protected abstract void subscribe(@NonNull final CompositeDisposable subscriptions);


}
