package com.spacebanana.funwithgeofence.mainmap;

import android.Manifest;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.ContextThemeWrapper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.spacebanana.funwithgeofence.utils.Constants;
import com.spacebanana.funwithgeofence.FunWithGeofenceApplication;
import com.spacebanana.funwithgeofence.geofence.GeofenceIntentService;
import com.spacebanana.funwithgeofence.R;

import javax.inject.Inject;

public class MainActivity extends FragmentActivity implements OnMapReadyCallback, MainMap {
    private static final int ACCESS_LOCATION_REQUEST_CODE = 929;
    private static final int GEOFENCE_REQUEST_CODE = 543;

    @Inject
    MainMapPresenter presenter;

    private GoogleMap googleMap;
    private Circle circle;
    private SeekBar.OnSeekBarChangeListener mRadiusSeekBarListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
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
            addGeofence(circle.getCenter(), circle.getRadius());
        }
    };

    private SeekBar radiusSeekBar;
    private TextView currentValueText;
    private TextView maxValueText;
    private TextView statusText;
    private RelativeLayout detailsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FunWithGeofenceApplication.get().getInjector().inject(this);
        setContentView(R.layout.activity_main);

        initMap();
        initViews();
        initSubscribers();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.add_network:
                showSetNetworkDialog();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showSetNetworkDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, R.style.AlertDialogTheme));
        builder.setTitle(R.string.set_network_name_title);

        final EditText input = new EditText(this);
        input.setPadding(40,40,40,40);
        input.setTextColor(Color.BLACK);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(presenter.getNetworkName());
        builder.setView(input);

        builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (!input.getText().toString().isEmpty())
                    presenter.setNetworkName(input.getText().toString());
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        builder.show();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;

        findCurrentLocationAndSetOnMap();

        this.googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setAreaOnMap(latLng, Constants.MIN_GEOFENCE_RADIUS);
            }
        });
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
    public void showGeofenceArea(double lat, double lon, int radius) {
        setAreaOnMap(new LatLng(lat, lon), radius);
    }

    @Override
    public void showGeofenceStatus(boolean isInsideZone) {
        if (isInsideZone)
            setViewsByStatus(R.drawable.status_inside_bg, getString(R.string.status_inside_title),
                    getString(R.string.status_inside));
        else
            setViewsByStatus(R.drawable.status_outside_bg, getString(R.string.status_outside_title),
                    getString(R.string.status_outside));
    }

    private void initSubscribers() {
        presenter.subscribeOnNetworkStateChange(this);
    }

    private void setAreaOnMap(LatLng latLng, int radius) {
        googleMap.clear();

        MarkerOptions options = new MarkerOptions()
                .position(latLng);

        googleMap.addMarker(options);

        CircleOptions circleOptions = new CircleOptions()
                .center(latLng)
                .radius(radius);

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
        radiusSeekBar = findViewById(R.id.radius_seek_bar);
        radiusSeekBar.setProgress(Constants.MIN_GEOFENCE_RADIUS);
        radiusSeekBar.setOnSeekBarChangeListener(mRadiusSeekBarListener);

        currentValueText = findViewById(R.id.current_value_text);
        currentValueText.setText(String.valueOf(Constants.MIN_GEOFENCE_RADIUS));

        maxValueText = findViewById(R.id.max_value_text);
        maxValueText.setText(String.valueOf(Constants.MAX_GEOFENCE_RADIUS));

        detailsLayout = findViewById(R.id.details_lt);

        statusText = findViewById(R.id.status_text);
    }

    private void setViewsByStatus(int drawable, String statusTitle, String status) {
        if (getActionBar() != null) {
            getActionBar().setTitle(statusTitle);
            getActionBar().setBackgroundDrawable(getResources().getDrawable(drawable, null));
        }

        detailsLayout.setBackground(getResources().getDrawable(drawable, null));
        statusText.setText(status);
    }

    private void findCurrentLocationAndSetOnMap() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                    ACCESS_LOCATION_REQUEST_CODE);
        } else {
            googleMap.setMyLocationEnabled(true);
            presenter.addGeofenceToCurrentLocation();
        }
    }

    private void addGeofence(LatLng point, Double radius) {
        if (presenter.getGeofencePendingIntent() == null){
            Intent intent = new Intent(this, GeofenceIntentService.class);
            PendingIntent pendingIntent = PendingIntent.getService(this, GEOFENCE_REQUEST_CODE, intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            presenter.setGeofencePendingIntent(pendingIntent);
        }

        presenter.addGeofenceArea(point, radius.intValue());
    }
}
