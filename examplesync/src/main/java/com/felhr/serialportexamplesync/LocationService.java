package com.example.lucas.location;

import android.app.Activity;
import android.location.Location;
import com.google.android.gms.location.*;

public class LocationService {
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationRequest mLocationRequest;
    private LocationCallback mLocationCallback;

    private double latitude;
    private double longitude;

    private void setLocationCallBack() {
        mLocationCallback = (new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult != null) {
                    for (Location location : locationResult.getLocations()) {
                        latitude  = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                }
            }
        });
    }

    private void createLocationRequest(long interval) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(interval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        /*
        // import com.google.android.gms.tasks.OnSuccessListener;
        // import com.google.android.gms.tasks.Task;
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        SettingsClient client = LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // Toast.makeText(MainActivity.this, "Settings are satisfied", Toast.LENGTH_SHORT).show();
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed by showing the user a dialog.
                    Toast.makeText(MainActivity.this, "Settings are NOT satisfied", Toast.LENGTH_SHORT).show();

                    try {
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(MainActivity.this,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {

                    }
                }
            }
        });
        */
    }

    /*
     |
     |  Public Functions
     |
    */

    public LocationService(Activity activity) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        setLocationCallBack();
        createLocationRequest(5000);

        this.latitude = 0;
        this.longitude = 0;
    }

    public LocationService(Activity activity, long interval) {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(activity);

        setLocationCallBack();
        createLocationRequest(interval);

        this.latitude = 0;
        this.longitude = 0;
    }

    public double getLatitude() {
        return this.latitude;
    }

    public double getLongitude() {
        return this.longitude;
    }

    public void pause() {
        // Stop Location Updates
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    public void resume() {
        // Start Location Updates
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }
}