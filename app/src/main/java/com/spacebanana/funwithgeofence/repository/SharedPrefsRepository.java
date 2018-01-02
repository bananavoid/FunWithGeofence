package com.spacebanana.funwithgeofence.repository;

import android.content.SharedPreferences;

import com.google.android.gms.maps.model.LatLng;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;

import javax.inject.Inject;

import static android.content.Context.MODE_PRIVATE;

public class SharedPrefsRepository {
    private static final String SHARED_PREFS = "area_shared_prefs";
    private static final String PREF_AREA_LAT = "area_lat";
    public static final String PREF_IS_IN_AREA = "is_in_area";
    public static final String PREF_IS_NETWORK_CONNECTED = "is_network_connected";
    private static final String PREF_AREA_LON = "area_lon";
    private static final String PREF_AREA_RADIUS = "area_radius";
    private static final String PREF_NETWORK_NAME = "network_name";

    private final SharedPreferences prefs;

    @Inject public SharedPrefsRepository() {
        prefs = FunWithGeofenceApplication.get().getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
    }

    public void setOnSharedPrefsListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        prefs.registerOnSharedPreferenceChangeListener(listener);
    }

    public boolean isNetworkConnected() {
        return this.prefs.getBoolean(PREF_IS_NETWORK_CONNECTED, false);
    }

    public void setIsNetworkConnected(boolean isNetworkConnected) {
        this.prefs.edit().putBoolean(PREF_IS_NETWORK_CONNECTED, isNetworkConnected).apply();
    }

    public boolean isInArea() {
        return this.prefs.getBoolean(PREF_IS_IN_AREA, false);
    }

    public void setIsInArea(boolean isInArea) {
        this.prefs.edit().putBoolean(PREF_IS_IN_AREA, isInArea).apply();
    }

    public double getAreaLatitude() {
        return Double.longBitsToDouble(this.prefs.getLong(PREF_AREA_LAT, Double.doubleToLongBits(0)));
    }

    public void setAreaLatitude(double value) {
        this.prefs.edit().putLong(PREF_AREA_LAT, Double.doubleToRawLongBits(value)).apply();
    }

    public double getAreaLontitude() {
        return Double.longBitsToDouble(this.prefs.getLong(PREF_AREA_LON, Double.doubleToLongBits(0)));
    }

    public void setAreaLontitude(float value) {
        this.prefs.edit().putLong(PREF_AREA_LON, Double.doubleToRawLongBits(value)).apply();
    }

    public int getAreaRadius() {
        return this.prefs.getInt(PREF_AREA_RADIUS, 0);
    }

    public void setAreaRadius(int value) {
        this.prefs.edit().putInt(PREF_AREA_RADIUS, value).apply();
    }

    public String getNetworkName() {
        return this.prefs.getString(PREF_NETWORK_NAME, "");
    }

    public void setNetworkName(String value) {
        this.prefs.edit().putString(PREF_NETWORK_NAME, value).apply();
    }

    public void saveLocationData(LatLng areaCenterPoint, int radius) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREF_AREA_LAT, Double.doubleToRawLongBits(areaCenterPoint.latitude));
        editor.putLong(PREF_AREA_LON, Double.doubleToRawLongBits(areaCenterPoint.longitude));
        editor.putInt(PREF_AREA_RADIUS, radius);
        editor.apply();
    }

    public void clearData() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(PREF_AREA_LAT, 0);
        editor.putLong(PREF_AREA_LON, 0);
        editor.putInt(PREF_AREA_RADIUS, 0);
        editor.apply();
    }
}
