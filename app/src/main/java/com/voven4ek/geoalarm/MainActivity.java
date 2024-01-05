package com.voven4ek.geoalarm;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.voven4ek.geoalarm.services.foreground.LocationTrackerService;

import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapListener;
import org.osmdroid.events.ScrollEvent;
import org.osmdroid.events.ZoomEvent;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapController;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private FloatingActionButton startButton;
    private MapController mapController;
    private MapView mapView;
    private final int REQUEST_PERMISSIONS_REQUEST_CODE = 1;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        setContentView(R.layout.activity_main);

        requestPermissionsIfNecessary(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET, android.Manifest.permission.ACCESS_COARSE_LOCATION, android.Manifest.permission.POST_NOTIFICATIONS, Manifest.permission.WRITE_EXTERNAL_STORAGE});

        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(this);

        mapView = findViewById(R.id.map);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        mapView.setBuiltInZoomControls(true);
        mapController = (MapController) mapView.getController();
        mapController.setZoom(15);
        MyLocationNewOverlay mLocationOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(this), mapView);
        mLocationOverlay.enableMyLocation();
        mapView.getOverlays().add(mLocationOverlay);
        final DisplayMetrics dm = getResources().getDisplayMetrics();
        ScaleBarOverlay mScaleBarOverlay = new ScaleBarOverlay(mapView);
        mScaleBarOverlay.setCentred(true);
        mScaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(mScaleBarOverlay);
        Marker marker = new Marker(mapView);
        marker.setPosition((GeoPoint) mapView.getMapCenter());
        mapView.addMapListener(
                new MapListener() {
                    @Override
                    public boolean onScroll(ScrollEvent event) {
                        marker.setPosition((GeoPoint) mapView.getMapCenter());
                        return false;
                    }

                    @Override
                    public boolean onZoom(ZoomEvent event) {
                        return false;
                    }
                }
        );
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(marker);
        GeoPoint point2 = new GeoPoint(51496994, -134733);
        mapController.setCenter(point2);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        ArrayList<String> permissionsToRequest = new ArrayList<>(Arrays.asList(permissions).subList(0, grantResults.length));
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    private void requestPermissionsIfNecessary(String[] permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                permissionsToRequest.add(permission);
            }
        }
        if (permissionsToRequest.size() > 0) {
            ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.start_button) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Intent intent = new Intent(this, LocationTrackerService.class);
                GeoPoint point = (GeoPoint) mapView.getMapCenter();
                intent.putExtra("latitude", point.getLatitude());
                intent.putExtra("longitude", point.getLongitude());
                startForegroundService(intent);
            }
        }
    }
}