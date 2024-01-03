package com.voven4ek.geoalarm;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import com.voven4ek.geoalarm.services.foreground.LocationTrackerService;

public class MainActivity extends AppCompatActivity {
    private final ActivityResultLauncher<String[]> pushNotificationPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
        granted -> {
            Log.d("pushNotificationPermissionLauncher", "granted: " + granted);
        }
    );

    ActivityResultLauncher<String[]> locationPermissionRequest = registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
        Boolean fineLocationGranted = result.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false);
        if (fineLocationGranted != null && fineLocationGranted) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(new Intent(this, LocationTrackerService.class));
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        pushNotificationPermissionLauncher.launch(new String[]{android.Manifest.permission.POST_NOTIFICATIONS});
        locationPermissionRequest.launch(new String[]{
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
}