package com.spacebanana.funwithgeofence;

import android.app.Application;

public class FunWithGeofenceApplication extends Application {
    private static FunWithGeofenceApplication mInstance;

    public static synchronized FunWithGeofenceApplication get() {
        return mInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }
}
