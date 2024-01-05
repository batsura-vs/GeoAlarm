package com.voven4ek.geoalarm.services.foreground;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.voven4ek.geoalarm.MainActivity;
import com.voven4ek.geoalarm.R;

public class LocationTrackerService extends Service implements LocationListener {
    private static final String CHANNEL_ID = "LocationTracker";
    private static final String CHANNEL_NAME = "LocationService";
    private static final int NOTIFICATION_ID = 1;
    private static final String TAG = "LocationTrackerService";
    LocationManager locationManager;
    private final Location destination = new Location("destination");
    private final Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
    private final Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notification);

    public LocationTrackerService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000L, 10F, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startLocationService(intent);
        return START_STICKY;
    }

    private void startLocationService(Intent intent) {
        Bundle bundle = intent.getExtras();
        assert bundle != null;
        destination.setLatitude(bundle.getDouble("latitude"));
        destination.setLongitude(bundle.getDouble("longitude"));
        createNotificationChannel();
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID).setContentTitle("Foreground Service").setContentText("SERVICE").setSmallIcon(R.drawable.ic_launcher_foreground).setContentIntent(pendingIntent).build();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(NOTIFICATION_ID, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
        } else {
            startForeground(NOTIFICATION_ID, notification);
        }
        onLocationChanged(destination);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {
        Log.d(TAG, "Destinition: " + destination);
        float distance = location.distanceTo(destination);
        Log.d(TAG, "onLocationChanged: " + distance);
        // distance in meters
        if (distance <= 100000) {
            ringtone.play();
        }
    }
}