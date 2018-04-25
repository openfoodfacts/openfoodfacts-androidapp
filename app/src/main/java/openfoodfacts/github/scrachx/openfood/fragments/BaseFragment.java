package openfoodfacts.github.scrachx.openfood.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import openfoodfacts.github.scrachx.openfood.R;
import openfoodfacts.github.scrachx.openfood.models.State;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshListener;
import openfoodfacts.github.scrachx.openfood.views.listeners.OnRefreshView;

public abstract class BaseFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, OnRefreshView {

    private SwipeRefreshLayout swipeRefreshLayout;
    private OnRefreshListener refreshListener;

    public BaseFragment() {
        super();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnRefreshListener) {
            refreshListener = (OnRefreshListener) context;
        }
    }

    public View createView(LayoutInflater inflater, ViewGroup container, int layoutId) {
        View view = inflater.inflate(layoutId, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefresh);
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(this);
        }
    }

    @Override
    public void onRefresh() {
        if (refreshListener != null) {
            refreshListener.onRefresh();
        }
    }

    @Override
    public void refreshView(State state) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }
}
