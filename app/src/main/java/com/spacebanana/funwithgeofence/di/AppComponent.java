package com.spacebanana.funwithgeofence.di;

import com.spacebanana.funwithgeofence.MainActivity;

import dagger.Component;

@Component(modules = AppModule.class)
public interface AppComponent {

    void inject(MainActivity activity);

}
