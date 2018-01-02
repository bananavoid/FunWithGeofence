package com.spacebanana.funwithgeofence;

import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.spacebanana.funwithgeofence.repository.SharedPrefsRepository;

import javax.inject.Inject;

public class GeofenceTransitionsIntentService extends IntentService {
    public static final String TAG = GeofenceTransitionsIntentService.class.getSimpleName();

    @Inject SharedPrefsRepository repository;

    public GeofenceTransitionsIntentService() {
        super(TAG);

        FunWithGeofenceApplication.get().getInjector().inject(this);
    }

    public GeofenceTransitionsIntentService(String name) {
        super(name);

        FunWithGeofenceApplication.get().getInjector().inject(this);
    }

    protected void onHandleIntent(Intent intent) {

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            // TODO handle error
            return;
        }

        int geofenceTransition = geofencingEvent.getGeofenceTransition();
        boolean storedAreaStatus = repository.isInArea();
        boolean currentStatus = geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_DWELL;

        if (storedAreaStatus != currentStatus)
            repository.setIsInArea(currentStatus);
    }
}
