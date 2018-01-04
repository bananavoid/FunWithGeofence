package com.spacebanana.funwithgeofence.repository;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;

import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.github.pwittchen.reactivenetwork.library.rx2.network.observing.strategy.LollipopNetworkObservingStrategy;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.geofence.GeofenceIntentService;
import com.spacebanana.funwithgeofence.geofence.GeofencePoint;
import com.spacebanana.funwithgeofence.utils.Constants;
import com.spacebanana.funwithgeofence.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class GeofenceRepository {
    private static final int GEOFENCE_REQUEST_CODE = 543;

    private final GeofencingClient geofencingClient;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final SharedPrefsUtils sharedPrefsUtils;
    private Disposable networkStateSubscription;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Geofence> geofenceList;

    @Inject
    public GeofenceRepository(GeofencingClient gfClient, FusedLocationProviderClient flProviderClient, SharedPrefsUtils sharedPrefsUtils) {
        this.geofencingClient = gfClient;
        this.fusedLocationProviderClient = flProviderClient;
        this.sharedPrefsUtils = sharedPrefsUtils;
    }

    public void defaultInit() {
        clearStoredLocationData();
        sharedPrefsUtils.setIsNetworkConnected(false);
        sharedPrefsUtils.setNetworkName("");
    }

    public Observable<Boolean> subscribeOnNetworkStateChange() {
        return Observable.create(e -> networkStateSubscription = ReactiveNetwork.observeNetworkConnectivity(
                FunWithGeofenceApplication.get().getApplicationContext(),
                new LollipopNetworkObservingStrategy())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(connectivity -> {
                    String networkSSID = connectivity.getExtraInfo().replace("\"", "");
                    if (getNetworkName().isEmpty())
                        setNetworkName(networkSSID);

                    if (networkSSID.equals(getNetworkName()) && connectivity.getType() == ConnectivityManager.TYPE_WIFI) {
                        setIsNetworkConnected(connectivity.isAvailable());
                    } else {
                        setIsNetworkConnected(false);
                    }

                    e.onNext(isInsideAreaOrConnected());
                }));
    }

    public Observable<Boolean> subscribeOnGeofenceAreaStatusChange() {
        return Observable.create(e ->
                sharedPrefsUtils.setOnSharedPrefsListener((sharedPreferences, s) -> {
                    if (s.equals(SharedPrefsUtils.PREF_IS_IN_AREA)) {
                        e.onNext(isInsideAreaOrConnected());
                    }
                })
        );
    }

    @SuppressWarnings("MissingPermission")
    public Observable<GeofencePoint> addGeofenceArea(double latitude, double longtitude, int radius) {
        return Observable.create(e -> fusedLocationProviderClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        double lat, lon;
                        if (latitude == 0 && longtitude == 0) {
                            lat = location.getLatitude();
                            lon = location.getLongitude();
                        } else {
                            lat = latitude;
                            lon = longtitude;
                        }

                        updateGeofenceArea(lat, lon, radius);

                        setIsInAreaByLocation(location);
                        e.onNext(new GeofencePoint(lat, lon, radius, isInsideAreaOrConnected()));
                    }
                }));
    }

    public Observable<Boolean> setNetworkName(String networkName) {
        return Observable.create(e -> {
            sharedPrefsUtils.setNetworkName(networkName);
            String currentlyConnectedTo = getConnectedNetworkName().replace("\"", "");
            setIsNetworkConnected(!currentlyConnectedTo.isEmpty() && currentlyConnectedTo.equals(networkName));
            e.onNext(isInsideAreaOrConnected());
        });
    }

    public String getNetworkName() {
        return sharedPrefsUtils.getNetworkName();
    }

    private void setIsNetworkConnected(boolean b) {
        sharedPrefsUtils.setIsNetworkConnected(b);
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent == null) {
            Intent intent = new Intent(FunWithGeofenceApplication.get().getApplicationContext(), GeofenceIntentService.class);
            geofencePendingIntent = PendingIntent.getService(FunWithGeofenceApplication.get().getApplicationContext(), GEOFENCE_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return geofencePendingIntent;
    }

    private boolean isInsideAreaOrConnected() {
        return sharedPrefsUtils.isNetworkConnected() || sharedPrefsUtils.isInArea();
    }

    public boolean getIsInsideArea() {
        return sharedPrefsUtils.isInArea();
    }

    public void setIsInsideArea(boolean b) {
        sharedPrefsUtils.setIsInArea(b);
    }

    private void clearStoredLocationData() {
        sharedPrefsUtils.clearData();
    }

    private String getConnectedNetworkName() {
        WifiManager wifiManager = (WifiManager) FunWithGeofenceApplication.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null ? wifiManager.getConnectionInfo().getSSID() : "";
    }

    private void setIsInAreaByLocation(Location location) {
        float[] distance = new float[2];
        Location.distanceBetween(location.getLatitude(), location.getLongitude(),
                sharedPrefsUtils.getAreaLatitude(), sharedPrefsUtils.getAreaLontitude(), distance);
        sharedPrefsUtils.setIsInArea(distance[0] < sharedPrefsUtils.getAreaRadius());
    }

    public Disposable getNetworkStateSubscription() {
        return networkStateSubscription;
    }

    @SuppressWarnings("MissingPermission")
    private void updateGeofenceArea(double latitude, double longitude, int radius) {
        //only one geofence is allowed, clearing up
        removeGeofences();

        Geofence geofence = new Geofence.Builder()
                .setRequestId(Constants.GEOFENCE_REQUEST_ID)
                .setCircularRegion(
                        latitude,
                        longitude,
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
        sharedPrefsUtils.saveLocationData(latitude, longitude, radius);
    }

    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        sharedPrefsUtils.clearData();
        geofencingClient.removeGeofences(getGeofencePendingIntent());

        if (geofenceList != null)
            geofenceList.clear();
    }

    private GeofencingRequest getGeofencingRequest(List<Geofence> geofenceList) {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER | GeofencingRequest.INITIAL_TRIGGER_EXIT);
        builder.addGeofences(geofenceList);
        return builder.build();
    }
}
