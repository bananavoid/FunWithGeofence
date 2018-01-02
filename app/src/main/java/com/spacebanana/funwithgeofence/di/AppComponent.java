package com.spacebanana.funwithgeofence.di;

import com.spacebanana.funwithgeofence.GeofenceTransitionsIntentService;
import com.spacebanana.funwithgeofence.mainmap.MainActivity;

import dagger.Component;

@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(MainActivity activity);
    void inject(GeofenceTransitionsIntentService service);
}
