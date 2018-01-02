package com.spacebanana.funwithgeofence;


import android.content.Context;
import android.net.ConnectivityManager;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.network.observing.strategy.LollipopNetworkObservingStrategy;
import com.spacebanana.funwithgeofence.repository.GeofenceRepository;
import com.spacebanana.funwithgeofence.rxviper.Presenter;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainMapPresenter extends Presenter<MainMap> {
    private final GeofenceRepository repository;
    private String networkName;
    private Disposable networkStateSubscription;

    @Inject
    public MainMapPresenter(GeofenceRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    protected void onTakeView(MainMap view) {
        super.onTakeView(view);
    }

    public void subscribeOnNetworkStateChange(Context context) {
        networkStateSubscription = ReactiveNetwork.observeNetworkConnectivity(context, new LollipopNetworkObservingStrategy())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connectivity>() {
                    @Override
                    public void accept(final Connectivity connectivity) {
                        String networkSSID = connectivity.getExtraInfo().toLowerCase().replace("\"", "");
                        if (networkSSID.equals(getNetworkName()) && connectivity.getType() == ConnectivityManager.TYPE_WIFI) {
                            getView().showGeofenceStatus(connectivity.isAvailable());
                        } else {
                            checkGeofenceArea();
                        }
                    }
                });
    }

    private void checkGeofenceArea() {
        getView().showGeofenceStatus(false);
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }

    public Disposable getNetworkStateSubscription() {
        return networkStateSubscription;
    }
}
