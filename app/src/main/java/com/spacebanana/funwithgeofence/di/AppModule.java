package com.spacebanana.funwithgeofence.di;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.LocationServices;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.utils.SharedPrefsUtils;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule {

    private FunWithGeofenceApplication application;

    public AppModule(FunWithGeofenceApplication application) {
        this.application = application;
    }

    @Provides FunWithGeofenceApplication provideApplication() {
        return application;
    }

    @Provides GeofencingClient provideGeofencingClient() {
        return LocationServices.getGeofencingClient(application);
    }

    @Provides FusedLocationProviderClient provideFusedLocationClient() {
        return LocationServices.getFusedLocationProviderClient(application);
    }

    @Provides
    SharedPrefsUtils provideSharedPrefsUtils() {
        return new SharedPrefsUtils();
    }
}
