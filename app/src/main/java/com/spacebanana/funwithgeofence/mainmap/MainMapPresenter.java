package com.spacebanana.funwithgeofence.mainmap;


import android.app.PendingIntent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;

import com.github.pwittchen.reactivenetwork.library.rx2.Connectivity;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.network.observing.strategy.LollipopNetworkObservingStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spacebanana.funwithgeofence.utils.Constants;
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
    private final FusedLocationProviderClient fusedLocationProviderClient;

    private Disposable networkStateSubscription;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Geofence> geofenceList;
    private boolean isConnected;

    @Inject
    public MainMapPresenter(SharedPrefsRepository repository, GeofencingClient gfClient, FusedLocationProviderClient flProviderClient) {
        super();
        this.repository = repository;
        this.geofencingClient = gfClient;
        this.fusedLocationProviderClient = flProviderClient;

        defaultInit();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (s.equals(SharedPrefsRepository.PREF_IS_IN_AREA)) {
            applyStatusChange();
        }
    }

    public void defaultInit() {
        repository.clearData();
        repository.setOnSharedPrefsListener(this);
    }

    public void setNetworkName(String networkName) {
        repository.setNetworkName(networkName);
    }

    public String getNetworkName() {
        return repository.getNetworkName();
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

    public void subscribeOnNetworkStateChange(Context context) {
        networkStateSubscription = ReactiveNetwork.observeNetworkConnectivity(context, new LollipopNetworkObservingStrategy())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Connectivity>() {
                    @Override
                    public void accept(final Connectivity connectivity) {
                        String networkSSID = connectivity.getExtraInfo().replace("\"", "");
                        if (getNetworkName().isEmpty())
                            setNetworkName(networkSSID);

                        if (networkSSID.equals(repository.getNetworkName()) && connectivity.getType() == ConnectivityManager.TYPE_WIFI) {
                            repository.setIsNetworkConnected(connectivity.isAvailable());
                        } else {
                            repository.setIsNetworkConnected(false);
                        }

                        applyStatusChange();
                    }
                });
    }

    @SuppressWarnings("MissingPermission")
    public void findCurrentLocation() {
        fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            getView().showGeofenceArea(location.getLatitude(), location.getLongitude(), Constants.MIN_GEOFENCE_RADIUS);
                        }
                    }
                });
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

        if (geofenceList == null)
            geofenceList = new ArrayList<>(1);

        geofenceList.add(geofence);

        geofencingClient.addGeofences(getGeofencingRequest(geofenceList), getGeofencePendingIntent());

        repository.saveLocationData(centralPoint, radius);
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        repository.clearData();
        geofencingClient.removeGeofences(getGeofencePendingIntent());

        if (geofenceList != null)
            geofenceList.clear();
    }

    private void applyStatusChange() {
        boolean isnet = repository.isNetworkConnected();
        boolean isarea = repository.isInArea();
        boolean isInTheArea = isnet || isarea;
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
}
