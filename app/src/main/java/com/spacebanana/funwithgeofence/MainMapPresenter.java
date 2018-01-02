package com.spacebanana.funwithgeofence;


import com.spacebanana.funwithgeofence.rxviper.Presenter;

import javax.inject.Inject;

public class MainMapPresenter extends Presenter<MainMap> {
    private final GeofenceRepository repository;
    private String networkName;

    @Inject
    NetworkStateManager networkStateManager;

    @Inject public MainMapPresenter(GeofenceRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    protected void onTakeView(MainMap view) {
        super.onTakeView(view);
        networkStateManager.subscribeOnNetworkChanges(getNetworkName());
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setNetworkName(String networkName) {
        this.networkName = networkName;
    }
}
