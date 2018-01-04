package com.spacebanana.funwithgeofence.mainmap;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.network.observing.strategy.LollipopNetworkObservingStrategy;
import com.google.android.gms.maps.model.LatLng;
import com.spacebanana.funwithgeofence.utils.Constants;
import com.spacebanana.funwithgeofence.repository.GeofenceRepository;
import com.spacebanana.funwithgeofence.rxviper.Presenter;
import com.spacebanana.funwithgeofence.utils.SharedPrefsUtils;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainMapPresenter extends Presenter<MainMap> implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final GeofenceRepository repository;

    private Disposable networkStateSubscription;
    private boolean isConnected;

    @Inject
    public MainMapPresenter(GeofenceRepository repository) {
        super();
        this.repository = repository;

        defaultInit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SharedPrefsUtils.PREF_IS_IN_AREA)) {
            applyStatusChange();
        }
    }

    public void setNetworkName(String networkName) {
        String currentlyConnectedTo = repository.getConnectedNetworkName().replace("\"", "");

        repository.setNetworkName(networkName);
        repository.setIsNetworkConnected(!currentlyConnectedTo.isEmpty() && currentlyConnectedTo.equals(networkName));
        applyStatusChange();
    }

    public String getNetworkName() {
        return repository.getNetworkName();
    }

    public Disposable getNetworkStateSubscription() {
        return networkStateSubscription;
    }

    public void subscribeOnNetworkStateChange(Context context) {
        networkStateSubscription = ReactiveNetwork.observeNetworkConnectivity(context, new LollipopNetworkObservingStrategy())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    String networkSSID = connectivity.getExtraInfo().replace("\"", "");
                    if (getNetworkName().isEmpty())
                        setNetworkName(networkSSID);

                    if (networkSSID.equals(repository.getNetworkName()) && connectivity.getType() == ConnectivityManager.TYPE_WIFI) {
                        repository.setIsNetworkConnected(connectivity.isAvailable());
                    } else {
                        repository.setIsNetworkConnected(false);
                    }

                    applyStatusChange();
                });
    }

    @SuppressWarnings("MissingPermission")
    public void addGeofenceToCurrentLocation() {
        repository.getFusedLocationProviderClient().getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null && getView() != null) {
                        getView().showGeofenceArea(location.getLatitude(), location.getLongitude(), Constants.MIN_GEOFENCE_RADIUS);
                    }
                });
    }

    @SuppressWarnings("MissingPermission")
    public void addGeofenceArea(LatLng centralPoint, int radius) {
        repository.updateGeofenceArea(centralPoint, radius);
        setIsCurrentLocationInArea();
        getView().showGeofenceArea(centralPoint.latitude, centralPoint.longitude, radius);
    }

    private void defaultInit() {
        repository.clearStoredLocationData();
        repository.setIsNetworkConnected(false);
        repository.setNetworkName("");
        repository.setOnSharedPrefsListener(this);
    }

    @SuppressWarnings("MissingPermission")
    private void setIsCurrentLocationInArea() {
        repository.getFusedLocationProviderClient().getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        repository.setIsInAreaByLocation(location);
                        applyStatusChange();
                    }
                });
    }

    private void applyStatusChange() {
        if (repository.isInsideAreaOrConnected() != isConnected) {
            isConnected = repository.isInsideAreaOrConnected();
            getView().showGeofenceStatus(isConnected);
        }
    }
}
