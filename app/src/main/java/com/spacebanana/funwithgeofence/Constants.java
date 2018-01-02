package com.spacebanana.funwithgeofence;

public class Constants {
    public static final int MIN_GEOFENCE_RADIUS = 150;
    public static final int MAX_GEOFENCE_RADIUS = 500;
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;

    public static final String GEOFENCE_REQUEST_ID = "area_51";
    public static final String ACTION_RECEIVE_GEOFENCE = "geofence_receive";
    public static final String EXTRA_GEOFENCE_TRANSITION = "geofence_transition";
}
