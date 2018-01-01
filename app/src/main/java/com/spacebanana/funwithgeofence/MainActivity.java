package com.spacebanana.funwithgeofence;

import android.Manifest;
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
import com.spacebanana.funwithgeofence.di.AppComponent;
import com.spacebanana.funwithgeofence.di.AppModule;
import com.spacebanana.funwithgeofence.di.DaggerAppComponent;

import javax.inject.Inject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, MainMap {
    private static final int ACCESS_LOCATION_REQUEST_CODE = 929;

    @Inject
    MainMapPresenter presenter;

    @Inject
    FusedLocationProviderClient fusedLocationProviderClient;

    private GoogleMap googleMap;
    private AppComponent component;
    private Circle circle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        component = (AppComponent) getLastCustomNonConfigurationInstance();
        if (component == null) {
            component = DaggerAppComponent.builder().appModule(new AppModule(getApplication())).build();
        }
        component.inject(this);

        setContentView(R.layout.activity_main);

        initMap();
        initViews();
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
    public Object onRetainCustomNonConfigurationInstance() {
        return component;
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

    }

    private void addMarkerOnMap(LatLng latLng) {
        // ALARM! Only one marker on map is allowed, clearing up
        googleMap.clear();

        MarkerOptions options = new MarkerOptions()
                .position(latLng);

        googleMap.addMarker(options);

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(100);

        circle = googleMap.addCircle(circleOptions);

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(new CameraPosition.Builder()
                .target(latLng)
                .zoom(15)
                .build()
        ));
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initViews() {
        SeekBar seekBar = findViewById(R.id.radius_seek_bar);
        seekBar.setProgress(150);
        TextView currentValueText = findViewById(R.id.current_value_text);
        currentValueText.setText(String.valueOf(150));

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
}
