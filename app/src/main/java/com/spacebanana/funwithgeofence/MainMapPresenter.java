package com.spacebanana.funwithgeofence;

import com.dzaitsev.rxviper.Presenter;

import javax.inject.Inject;

public class MainMapPresenter extends Presenter<MainMap> {
    private final GeofenceRepository repository;

    @Inject public MainMapPresenter(GeofenceRepository repository) {
        super();
        this.repository = repository;
    }


}
