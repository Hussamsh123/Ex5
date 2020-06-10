package com.hussam.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class LocationInfo {
    private MainActivity activity;
    private double locationAccuracy, locationLongitude, locationLatitude;
    private static final int INTERVAL = 10000;
    private static final int FASTEST_INTERVAL = 5000;
    private LocationRequest locationRequest;
    private LocationManager locationManager;
    private FusedLocationProviderClient locationProviderClient;
    private LocationCallback locationCallBack;
    private LocationListener locationListener;
    private static final String NEWLOCATION = "new_location";
    private static final String STOPPED = "stopped";
    private static final String STARTED = "started";
    private boolean track;

    LocationInfo(MainActivity context) {
        activity = context;
        createLocationRequest();
        createLocationCallBack();
        track = true;
        locationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


    }

    void startTracking() {
        track = true;
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (track) {
                    updateValues(location);
                    activity.sendBroadcast(new Intent("new_location"));
                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                activity.startActivity(intent);
            }

        };
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission
                (activity, Manifest.permission.ACCESS_COARSE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {

            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates("gps", FASTEST_INTERVAL, 0, locationListener);
    }

    void stopTracking(){
        track = false;
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(INTERVAL);
        locationRequest.setFastestInterval(FASTEST_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

    }

    private void createLocationCallBack(){
        locationCallBack = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Location location = locationResult.getLastLocation();
                updateValues(location);
            }
        };
    }
    private void updateValues(Location location){
        locationAccuracy = location.getAccuracy();
        locationLatitude = location.getLatitude();
        locationLongitude = location.getLongitude();
    }

    double getLatitude(){
        return locationLatitude;
    }
    double getLongitude(){
        return locationLongitude;
    }
    double getAccuracy(){
        return locationAccuracy;
    }

}
