package com.spacebanana.funwithgeofence;

public class Constants {
    public static final int MIN_GEOFENCE_RADIUS = 150;
    public static final int MAX_GEOFENCE_RADIUS = 500;
    public static final long GEOFENCE_EXPIRATION_H = 12;
    public static final long GEOFENCE_EXPIRATION_MS =
            GEOFENCE_EXPIRATION_H * 60 * 60 * 1000;

    public static final String GEOFENCE_REQUEST_ID = "area_51";
}
