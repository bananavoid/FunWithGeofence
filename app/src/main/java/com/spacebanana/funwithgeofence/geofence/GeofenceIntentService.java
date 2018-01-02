package com.spacebanana.funwithgeofence.geofence;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.repository.SharedPrefsRepository;

import javax.inject.Inject;

public class GeofenceIntentService extends IntentService {
    public static final String TAG = GeofenceIntentService.class.getSimpleName();

    @Inject SharedPrefsRepository repository;

    public GeofenceIntentService() {
        super(TAG);

        FunWithGeofenceApplication.get().getInjector().inject(this);
    }

    public GeofenceIntentService(String name) {
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
