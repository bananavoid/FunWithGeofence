package com.spacebanana.funwithgeofence.mainmap;


import com.spacebanana.funwithgeofence.rxviper.ViewCallbacks;

public interface MainMap extends ViewCallbacks {

    void showGeofenceArea(double lat, double lon, int radius);
    void showGeofenceStatus(boolean isInsideZone);
}
