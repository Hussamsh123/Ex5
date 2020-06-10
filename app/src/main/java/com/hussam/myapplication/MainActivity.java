package com.hussam.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity{
    private static final String NEWLOCATION = "new_location";
    private static final String STOPPED = "stopped";
    private static final String STARTED = "started";
    public static final String START_TRACKING_LOCATION = "Start tracking location";
    public static final String STOP_TRACKING_LOCATION = "Stop tracking location";
    public static final int METERS = 50;
    Button trackButton, setHomeButton, clearHomeButton;
    TextView longitudeValue, longitudeText,
            latitudeText, accuracyText, latitudeValue,
            accuracyValue, homeLocationText, homeLatitude,
            homeLongitude;
    private static final int locationRequestCode = 1111;
    private LocationInfo locationInfo;
    BroadcastReceiver broadcastReceiver;
    IntentFilter updateIntent, stopIntent, startIntent;
    private SharedPreferences sp;
    boolean showhome;
    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showhome = false;
        setContentView(R.layout.activity_main);
        sp  = PreferenceManager.getDefaultSharedPreferences(this);
        longitudeValue = findViewById(R.id.longitudeVal);
        latitudeValue = findViewById(R.id.latitudeVal);
        accuracyValue = findViewById(R.id.accuracyVal);
        accuracyText = findViewById(R.id.accuracyText);
        longitudeText = findViewById(R.id.longitudeText);
        latitudeText = findViewById(R.id.latitudeText);
        trackButton = findViewById(R.id.trackButton);
        setHomeButton = findViewById(R.id.homeButton);
        homeLocationText = findViewById(R.id.homeLocation);
        homeLatitude = findViewById(R.id.homeLatitude);
        homeLongitude = findViewById(R.id.homeLongitude);
        clearHomeButton = findViewById(R.id.clearHome);
        locationInfo = new LocationInfo(this);
        retrieveLocation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        trackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (trackButton.getText().toString().equals("Start tracking location")){
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        locationInfo.startTracking();
                        makeTextVisible();
                        trackButton.setText(STOP_TRACKING_LOCATION);

                }else {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                locationRequestCode);
                    }

                }else{
                    locationInfo.stopTracking();
                    makeTextInvisible();
                    trackButton.setText(START_TRACKING_LOCATION);
                }
            }
        });
        receiveBroadCasts();
        registerContextReceiver();
        setHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveHomeLocation();
                clearHomeButton.setVisibility(View.VISIBLE);
            }
        });
        clearHomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearHomeLocation();
            }
        });
        if (showhome){
            clearHomeButton.setVisibility(View.VISIBLE);
            homeLocationText.setVisibility(View.VISIBLE);
            homeLongitude.setVisibility(View.VISIBLE);
            homeLatitude.setVisibility(View.VISIBLE);
        }
    }

    void registerContextReceiver(){
        updateIntent = new IntentFilter(NEWLOCATION);
        stopIntent = new IntentFilter(STOPPED);
        startIntent = new IntentFilter(STARTED);
        registerReceiver(broadcastReceiver, updateIntent);
        registerReceiver(broadcastReceiver, startIntent);
        registerReceiver(broadcastReceiver, stopIntent);
    }


    void makeTextVisible(){
        latitudeValue.setVisibility(View.VISIBLE);
        longitudeValue.setVisibility(View.VISIBLE);
        accuracyValue.setVisibility(View.VISIBLE);
        latitudeText.setVisibility(View.VISIBLE);
        longitudeText.setVisibility(View.VISIBLE);
        accuracyText.setVisibility(View.VISIBLE);
    }

    void makeTextInvisible(){
        latitudeValue.setVisibility(View.GONE);
        longitudeValue.setVisibility(View.GONE);
        accuracyValue.setVisibility(View.GONE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == locationRequestCode) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                locationInfo.startTracking();
                updateUIValues(locationInfo);
                makeTextVisible();
            } else {
                Toast toast = Toast.makeText(this,
                        "You can't run this application without location" +
                                " services allowed", Toast.LENGTH_SHORT);

                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
            }
        }
    }
    @Override
    protected void onStop() {
        super.onStop();
        unregisterReceiver(broadcastReceiver);
    }



    void updateUIValues(LocationInfo location){
        latitudeValue.setText(String.valueOf(location.getLatitude()));
        longitudeValue.setText(String.valueOf(location.getLongitude()));
        accuracyValue.setText(String.valueOf(location.getAccuracy()));
    }
    void receiveBroadCasts(){
        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent == null){
                    return;
                }
                if (intent.getAction().equals(NEWLOCATION)){
                    updateUIValues(locationInfo);
                    makeTextVisible();
                    if(locationInfo.getAccuracy() <= METERS){
                        setHomeButton.setVisibility(View.VISIBLE);
                    }
                }

            }
        };
    }
    void saveLocation(){
        Gson gson =  new Gson();
        String acc = gson.toJson(accuracyValue.getText().toString());
        String lon = gson.toJson(longitudeValue.getText().toString());
        String lat = gson.toJson(latitudeValue.getText().toString());
        sp.edit().putString("acc", acc).putString("lon", lon).putString("lat", lat).apply();
    }

    void retrieveLocation(){
        Gson gson = new Gson();
        String acc = sp.getString("acc", "");
        String lat = sp.getString("lat", "");
        String lon = sp.getString("lon", "");
        accuracyValue.setText(acc);
        latitudeValue.setText(lat);
        longitudeValue.setText(lon);
        if (!lat.equals("")){
            showhome = true;
            homeLongitude.setText("Longitude: " + lon);
            homeLatitude.setText("Latitude: " +lat);
        }
//        gson.fromJson(acc, String.class)
    }
    void saveHomeLocation(){
        homeLongitude.setText("Longitude: " + longitudeValue.getText().toString());
        homeLatitude.setText("Latitude: " + latitudeValue.getText().toString());
        saveLocation();
        homeLocationText.setVisibility(View.VISIBLE);
        homeLongitude.setVisibility(View.VISIBLE);
        homeLatitude.setVisibility(View.VISIBLE);
    }
    void clearHomeLocation(){
        sp.edit().clear().apply();
        clearHomeButton.setVisibility(View.GONE);
        homeLongitude.setVisibility(View.GONE);
        homeLatitude.setVisibility(View.GONE);
        homeLocationText.setVisibility(View.GONE);
    }
}
