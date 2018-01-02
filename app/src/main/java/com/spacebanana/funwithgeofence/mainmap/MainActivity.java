package com.spacebanana.funwithgeofence.mainmap;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.spacebanana.funwithgeofence.Constants;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.geofence.GeofenceTransitionsIntentService;
import com.spacebanana.funwithgeofence.R;

import javax.inject.Inject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, MainMap {
    private static final int ACCESS_LOCATION_REQUEST_CODE = 929;
    private static final int GEOFENCE_REQUEST_CODE = 543;

    @Inject
    MainMapPresenter presenter;

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap googleMap;
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FunWithGeofenceApplication.get().getInjector().inject(this);
        setContentView(R.layout.activity_main);

        initMap();
        initViews();
        initSubscribers();
    }

    private void initSubscribers() {
        presenter.subscribeOnNetworkStateChange(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        presenter.takeView(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        presenter.dropView(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (presenter.getNetworkStateSubscription() != null)
            presenter.getNetworkStateSubscription().dispose();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        findCurrentLocationAndSetOnMap();

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                addMarkerOnMap(latLng);
            }
        });

        findViewById(R.id.seek_bar_lt).setVisibility(View.VISIBLE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (permissions.length > 0 &&
                    permissions[0].equals(Manifest.permission.ACCESS_FINE_LOCATION) &&
                    permissions[1].equals(Manifest.permission.ACCESS_COARSE_LOCATION) &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                    grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                findCurrentLocationAndSetOnMap();
            }
        }
    }

    @Override
    public void showGeofenceParams(long lat, long lon, int radius) {

    }

    @Override
    public void showGeofenceStatus(boolean isInsideZone) {
        getActionBar().setTitle(isInsideZone ? "CONNECTED" : "DISCONNECTED");
    }

    private void addMarkerOnMap(LatLng latLng) {
        // ALARM! Only one marker on map is allowed, clearing up
        googleMap.clear();

        MarkerOptions options = new MarkerOptions()
                .position(latLng);

        googleMap.addMarker(options);

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
//                .fillColor(Color.argb(128, 255, 0, 0))
                .radius(Constants.MIN_GEOFENCE_RADIUS);

        circle = googleMap.addCircle(circleOptions);

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build()
        ));

        addGeofence(latLng, circle.getRadius());
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initViews() {
        getActionBar().setTitle("NO STATUS");
        presenter.setNetworkName("spacelobster");

        SeekBar seekBar = findViewById(R.id.radius_seek_bar);
        seekBar.setProgress(Constants.MIN_GEOFENCE_RADIUS);
        TextView currentValueText = findViewById(R.id.current_value_text);
        currentValueText.setText(String.valueOf(Constants.MIN_GEOFENCE_RADIUS));

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView currentValueText = findViewById(R.id.current_value_text);
                currentValueText.setText(String.valueOf(i));

                if (circle != null) {
                    circle.setRadius(i);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    private void findCurrentLocationAndSetOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_LOCATION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null && googleMap != null) {
                                addMarkerOnMap(new LatLng(location.getLatitude(), location.getLongitude()));
                            }
                        }
                    });
        }
    }

    private void addGeofence(LatLng point, Double radius) {
        if (presenter.getGeofencePendingIntent() == null){
            Intent intent = new Intent(this, GeofenceTransitionsIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, GEOFENCE_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            presenter.setGeofencePendingIntent(pendingIntent);
        }

        presenter.addGeofenceArea(point, radius.intValue());
    }
}
