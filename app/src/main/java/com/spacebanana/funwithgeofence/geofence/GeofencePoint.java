package com.spacebanana.funwithgeofence.geofence;

/**
 * Created by spacebanana on 1/4/18.
 */

public class GeofencePoint {
    double lat;
    double lon;
    int radius;
    boolean isInArea;

    public GeofencePoint(double lat, double lon, int radius, boolean isInArea) {
        this.lat = lat;
        this.lon = lon;
        this.radius = radius;
        this.isInArea = isInArea;
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public boolean isInArea() {
        return isInArea;
    }

    public void setInArea(boolean inArea) {
        isInArea = inArea;
    }
}
