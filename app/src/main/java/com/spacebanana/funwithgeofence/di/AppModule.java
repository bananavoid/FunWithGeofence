package com.spacebanana.funwithgeofence.di;

import android.app.Application;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private Application application;

    public AppModule(Application application) {
        this.application = application;
    }

    @Provides Application provideApplication() {
        return application;
    }

    @Provides GeofencingClient provideGeofencingClient() {
        return LocationServices.getGeofencingClient(application);
    }

    @Provides FusedLocationProviderClient provideFusedLocationClient() {
        return LocationServices.getFusedLocationProviderClient(application);
    }
}
