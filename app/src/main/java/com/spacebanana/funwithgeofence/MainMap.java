package com.spacebanana.funwithgeofence;


import com.spacebanana.funwithgeofence.rxviper.ViewCallbacks;

public interface MainMap extends ViewCallbacks {

    void showGeofenceParams(long lat, long lon, int radius);

    void showGeofenceStatus(boolean isInsideZone);
}
