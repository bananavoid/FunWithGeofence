package com.spacebanana.funwithgeofence;

import com.dzaitsev.rxviper.ViewCallbacks;

public interface MainMap extends ViewCallbacks {

    void showGeofenceParams(long lat, long lon, int radius);

    void showGeofenceStatus(boolean isInsideZone);

}
