package com.spacebanana.funwithgeofence.di;

import com.spacebanana.funwithgeofence.MainActivity;

import dagger.Component;

@Component
public interface AppComponent {

    void inject(MainActivity activity);

}
