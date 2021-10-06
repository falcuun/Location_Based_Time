package com.example.logitudetimezone;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;
import android.widget.Toast;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    private TextView LocationView;
    private TextView calculatedTimeView;
    private TextView currentRealTimeView;

    private boolean isChangingUi = false;
    private boolean isTimerInitialized = false;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationView = findViewById(R.id.locationView);
        calculatedTimeView = findViewById(R.id.actualTimeView);
        currentRealTimeView = findViewById(R.id.currentRealTimeView);

        requestLocationPermission();
        LocationManager mLocationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
            return;
        }
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                200, 1, mLocationListener);

        checkTimeEverySecond_Thread();
    }

    private void checkTimeEverySecond_Thread() {
        Handler handler = new Handler();

        Runnable runnable = new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void run() {

                int hour = Instant.now().atZone(ZoneOffset.systemDefault()).getHour();
                int minute = Instant.now().atZone(ZoneOffset.systemDefault()).getMinute();
                int second = Instant.now().atZone(ZoneOffset.systemDefault()).getSecond();


                String[] timeUnits = {
                        hour < 10 ? "0" + hour : Integer.toString(hour),
                        minute < 10 ? "0" + minute : Integer.toString(minute),
                        second < 10 ? "0" + second : Integer.toString(second),
                };

                String realTime = String.format("Current Time: %s:%s:%s", timeUnits[0], timeUnits[1], timeUnits[2]);
                currentRealTimeView.setText(realTime);
                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

    }

    private final LocationListener mLocationListener = new LocationListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onLocationChanged(final Location location) {
            if (!isTimerInitialized) {
                isTimerInitialized = true;
                checkTimeEverySecond_Thread();
            }

            String loc = " Lat: " + location.getLatitude() + " Long: " + location.getLongitude();
            LocationView.setText(loc);
            long timeDifference = Utils.calculateTimeDifference(location.getLongitude());
            Instant currentCalculatedTime = Utils.calculateActualTime(timeDifference);

            int hour = currentCalculatedTime.atZone(ZoneOffset.UTC).getHour();
            int minute = currentCalculatedTime.atZone(ZoneOffset.UTC).getMinute();
            int second = currentCalculatedTime.atZone(ZoneOffset.UTC).getSecond();


            String[] timeUnits = {
                    hour < 10 ? "0" + hour : Integer.toString(hour),
                    minute < 10 ? "0" + minute : Integer.toString(minute),
                    second < 10 ? "0" + second : Integer.toString(second),
            };

            String calculatedTime = String.format("Location Based Time: %s:%s:%s", timeUnits[0], timeUnits[1], timeUnits[2]);
            if (isChangingUi)
                return;
            isChangingUi = true;
            calculatedTimeView.setText(calculatedTime);
            isChangingUi = false;
        }
    };


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "Permission Granted", Toast.LENGTH_LONG).show();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void requestLocationPermission() {
        ActivityResultLauncher<String[]> locationPermissionRequest =
                registerForActivityResult(new ActivityResultContracts
                                .RequestMultiplePermissions(), result -> {
                            Boolean fineLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_FINE_LOCATION, false);
                            Boolean coarseLocationGranted = result.getOrDefault(
                                    Manifest.permission.ACCESS_COARSE_LOCATION, false);


                        }
                );
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        });
    }
}


