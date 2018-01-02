package com.spacebanana.funwithgeofence.mainmap;


import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.network.observing.strategy.LollipopNetworkObservingStrategy;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.spacebanana.funwithgeofence.Constants;
import com.spacebanana.funwithgeofence.repository.SharedPrefsRepository;
import com.spacebanana.funwithgeofence.rxviper.Presenter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class MainMapPresenter extends Presenter<MainMap> implements SharedPreferences.OnSharedPreferenceChangeListener {
    private final SharedPrefsRepository repository;
    private final GeofencingClient geofencingClient;
    private String networkName;
    private Disposable networkStateSubscription;
    private PendingIntent geofencePendingIntent;
    private boolean isConnected;

    @Inject
    public MainMapPresenter(SharedPrefsRepository repository, GeofencingClient gfClient) {
        super();
        this.repository = repository;
        this.geofencingClient = gfClient;
    }

    @Override
    protected void onTakeView(MainMap view) {
        super.onTakeView(view);
        repository.setOnSharedPrefsListener(this);
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
                            repository.setIsNetworkConnected(connectivity.isAvailable());
                        } else {
                            repository.setIsNetworkConnected(false);
                        }

                        applyStatusChange();
                    }
                });
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
        repository.setNetworkName(networkName);
    }

    public Disposable getNetworkStateSubscription() {
        return networkStateSubscription;
    }

    public PendingIntent getGeofencePendingIntent() {
        return geofencePendingIntent;
    }

    public void setGeofencePendingIntent(PendingIntent geofencePendingIntent) {
        this.geofencePendingIntent = geofencePendingIntent;
    }

    @SuppressWarnings("MissingPermission")
    public void addGeofenceArea(LatLng centralPoint, int radius) {
        //only one geofence are is allowed, clearing up
        removeGeofences();

        Geofence geofence = new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        centralPoint.latitude,
                        centralPoint.longitude,
                        radius
                )
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_MS)
                .build();

        List<Geofence> geofenceList = new ArrayList<>(1);
        geofenceList.add(geofence);

        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getGeofencePendingIntent());

        repository.saveLocationData(centralPoint, radius);
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        repository.clearLocationData();
        geofencingClient.removeGeofences(getGeofencePendingIntent());
    }

    private void applyStatusChange() {
        boolean isInTheArea = repository.isNetworkConnected() && repository.isInArea() || repository.isNetworkConnected();
        if (isInTheArea != isConnected) {
            isConnected = isInTheArea;
            getView().showGeofenceStatus(isConnected);
        }
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER|GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SharedPrefsRepository.PREF_IS_IN_AREA)) {
            applyStatusChange();
        }
    }
}
