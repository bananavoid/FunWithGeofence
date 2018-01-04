package com.spacebanana.funwithgeofence.mainmap;


import com.spacebanana.funwithgeofence.repository.GeofenceRepository;
import com.spacebanana.funwithgeofence.rxviper.Presenter;

import javax.inject.Inject;

import io.reactivex.disposables.Disposable;

public class MainMapPresenter extends Presenter<MainMap>  {
    private final GeofenceRepository repository;

    @Inject
    public MainMapPresenter(GeofenceRepository repository) {
        super();
        this.repository = repository;
    }

    @Override
    protected void onTakeView(MainMap view) {
        super.onTakeView(view);
        initSubscribers();
    }

    private void initSubscribers() {
        repository.subscribeOnNetworkStateChange().subscribe(
                aBoolean -> getView().showGeofenceStatus(aBoolean)
        );
    }

    void setNetworkName(String networkName) {
        repository.setNetworkName(networkName).subscribe(aBoolean -> getView().showGeofenceStatus(aBoolean));
    }

    String getNetworkName() {
        return repository.getNetworkName();
    }

    Disposable getNetworkStateSubscription() {
        return repository.getNetworkStateSubscription();
    }

    void updateGeofenceArea(double lat, double lon, int radius) {
        repository.addGeofenceArea(lat, lon, radius).subscribe(
                point -> {
                    getView().showGeofenceArea(point.getLat(), point.getLon(), radius);
                    getView().showGeofenceStatus(point.isInArea());
                }
        );
    }
}
