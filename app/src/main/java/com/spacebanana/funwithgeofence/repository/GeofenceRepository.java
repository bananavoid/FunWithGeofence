package com.spacebanana.funwithgeofence.repository;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.wifi.WifiManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.geofence.GeofenceIntentService;
import com.spacebanana.funwithgeofence.utils.Constants;
import com.spacebanana.funwithgeofence.utils.SharedPrefsUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class GeofenceRepository {
    private static final int GEOFENCE_REQUEST_CODE = 543;

    private final GeofencingClient geofencingClient;
    private final FusedLocationProviderClient fusedLocationProviderClient;
    private final SharedPrefsUtils sharedPrefsUtils;
    private PendingIntent geofencePendingIntent;
    private ArrayList<Geofence> geofenceList;

    @Inject public GeofenceRepository(GeofencingClient gfClient, FusedLocationProviderClient flProviderClient, SharedPrefsUtils sharedPrefsUtils) {
        this.geofencingClient = gfClient;
        this.fusedLocationProviderClient = flProviderClient;
        this.sharedPrefsUtils = sharedPrefsUtils;
    }

    public void setNetworkName(String networkName) {
        sharedPrefsUtils.setNetworkName(networkName);
    }

    public void setIsNetworkConnected(boolean b) {
        sharedPrefsUtils.setIsNetworkConnected(b);
    }

    public String getNetworkName() {
        return sharedPrefsUtils.getNetworkName();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (geofencePendingIntent == null){
            Intent intent = new Intent(FunWithGeofenceApplication.get().getApplicationContext(), GeofenceIntentService.class);
            geofencePendingIntent = PendingIntent.getService(FunWithGeofenceApplication.get().getApplicationContext(), GEOFENCE_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
        }

        return geofencePendingIntent;
    }

    public FusedLocationProviderClient getFusedLocationProviderClient() {
        return fusedLocationProviderClient;
    }

    public boolean isInsideAreaOrConnected() {
        return sharedPrefsUtils.isNetworkConnected() || sharedPrefsUtils.isInArea();
    }

    public boolean getIsInsideArea() {
        return sharedPrefsUtils.isInArea();
    }

    public void setIsInsideArea(boolean b) {
        sharedPrefsUtils.setIsInArea(b);
    }

    public void clearStoredLocationData() {
        sharedPrefsUtils.clearData();
    }

    public void setOnSharedPrefsListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPrefsUtils.setOnSharedPrefsListener(listener);
    }

    public String getConnectedNetworkName() {
        WifiManager wifiManager = (WifiManager) FunWithGeofenceApplication.get().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        return wifiManager != null ? wifiManager.getConnectionInfo().getSSID() : "";
    }

    public void setIsInAreaByLocation(Location currentLocation) {
        float[] distance = new float[2];

        Location.distanceBetween(currentLocation.getLatitude(), currentLocation.getLongitude(),
                sharedPrefsUtils.getAreaLatitude(), sharedPrefsUtils.getAreaLontitude(), distance);

        sharedPrefsUtils.setIsInArea(distance[0] < sharedPrefsUtils.getAreaRadius());
    }

    @SuppressWarnings("MissingPermission")
    public void updateGeofenceArea(LatLng centralPoint, int radius) {
        //only one geofence is allowed, clearing up
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
        sharedPrefsUtils.saveLocationData(centralPoint, radius);
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
