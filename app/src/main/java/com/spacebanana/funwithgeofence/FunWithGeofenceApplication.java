package com.spacebanana.funwithgeofence;

import android.app.Application;

import com.spacebanana.funwithgeofence.di.AppComponent;
import com.spacebanana.funwithgeofence.di.AppModule;
import com.spacebanana.funwithgeofence.di.DaggerAppComponent;

public class FunWithGeofenceApplication extends Application {
    private static FunWithGeofenceApplication appInstance;
    private AppComponent component;

    public static synchronized FunWithGeofenceApplication get() {
        return appInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        appInstance = this;

        if (component == null) {
            component = DaggerAppComponent.builder().appModule(new AppModule(FunWithGeofenceApplication.get())).build();
        }
    }

    public AppComponent getInjector() {
        return component;
    }
}
