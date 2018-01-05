package com.spacebanana.funwithgeofence.di;

import com.spacebanana.funwithgeofence.geofence.GeofenceIntentService;
import com.spacebanana.funwithgeofence.mainmap.MainActivity;
import com.spacebanana.funwithgeofence.repository.GeofenceRepository;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = AppModule.class)
public interface AppComponent {
    void inject(MainActivity activity);
    void inject(GeofenceIntentService service);
}
