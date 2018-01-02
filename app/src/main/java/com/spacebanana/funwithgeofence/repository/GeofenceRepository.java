package com.spacebanana.funwithgeofence.repository;

import com.google.android.gms.location.GeofencingClient;

import javax.inject.Inject;

public class GeofenceRepository {

    private final GeofencingClient geofencingClient;

    @Inject public GeofenceRepository(GeofencingClient geofencingClient) {
        this.geofencingClient = geofencingClient;
    }
}
