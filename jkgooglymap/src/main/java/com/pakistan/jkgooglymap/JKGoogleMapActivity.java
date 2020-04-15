package com.pakistan.jkgooglymap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

public abstract class JKGoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "JKGoogleMapActivity";

    private static final int REQUEST_PERMISSION_CODE = 1625;
    private static final int REQUEST_PLAY_SERVICES_CODE = 3474;
    private static final int GPS_REQUEST_CODE = 7665;
    private static final float ZOOM_LEVEL = 14f;

    protected static final String[] PERMISSIONS = new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
    };


    // Abstract Methods
    public abstract void onGranted();
    public abstract void onDenied();
    public abstract void onGoogleMapReady(GoogleMap map);
    protected abstract void onCurrentLocationFound(Location currentLocation, String msg);
    protected abstract void onPlaceSearched(Place place, String msg);

    // Google References
    private PlaceAutocompleteFragment mPlaceAutocompleteFragment;
    protected SupportMapFragment mSupportMapFragment;
    protected FusedLocationProviderClient mFusedLocationProviderClient;


    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    // Check, if Google play services available or not
    protected boolean hasPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int result = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (result == ConnectionResult.SUCCESS)
            return true;
        else if (googleApiAvailability.isUserResolvableError(result)) {
            Dialog dialog = googleApiAvailability.getErrorDialog(this, result, REQUEST_PLAY_SERVICES_CODE, DialogInterface::dismiss);
            dialog.show();
        } else
            Toast.makeText(this, "Play Services are required for this application", Toast.LENGTH_SHORT).show();

        return false;
    }

    // find Current Device Location
    protected void findCurrentDeviceLocation() {
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mFusedLocationProviderClient.getLastLocation().addOnCompleteListener(task -> {
            Location currentLocation = null;
            String msg = "Unable to find Current Device Location";

            if(task.isSuccessful()) {
                currentLocation = task.getResult();
                msg = "Current Location found";
            }

            onCurrentLocationFound(currentLocation, msg);
        });
    }

    // check if GPS is enabled or not
    protected boolean isGPSEnabled() {
        boolean enabled = false;
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            enabled = true;

        return enabled;
    }

    // Show dialog to enable GPS
    protected void showGPSDialogBox(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("GPS Permission");
        builder.setMessage("GPS needs to be enabled for accuracy. Would you like to enable GPS?");
        builder.setPositiveButton("Enable GPS", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, GPS_REQUEST_CODE);
            dialog.dismiss();
        });

        builder.create().show();
    }

    // Add Marker to specified location
    protected Marker addMarkerTo(GoogleMap map, LatLng latLng, String title, String snippet, int icon) {
        MarkerOptions options = new MarkerOptions();
        options.title(title);
        options.snippet(snippet);
        options.position(latLng);
        options.icon(BitmapDescriptorFactory.fromResource(icon));

        return map.addMarker(options);
    }

    // Animate Camera to specified location
    protected void animateCameraTo(GoogleMap map, LatLng latLng){
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(latLng, ZOOM_LEVEL);
        map.moveCamera(cameraUpdate);
    }

    // initialize Google Places
    protected void initGooglePlaces(int placeResId){
        mPlaceAutocompleteFragment = (PlaceAutocompleteFragment) getFragmentManager().findFragmentById(placeResId);
        mPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                Log.d(TAG, "_on_Google_PlaceSelected: "+place);
                if(place != null)
                    onPlaceSearched(place, "Searched Place found");
                else
                    onPlaceSearched(null, "Searched Place not found");
            }

            @Override
            public void onError(Status status) {
                Log.e(TAG, "_on_Google_PlaceSelected_Error: "+status.toString());
                onPlaceSearched(null, status.toString());
            }
        });
    }

    // initialize Google Map
    protected void initGoogleMap(int mapResId){
        mSupportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(mapResId);
        mSupportMapFragment.getMapAsync(this);
    }

    // check and Request Runtime Permissions for Google Map
    protected void requestRuntimePermissions() {
        if (!isPermissionEnabled(PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION_CODE);
        }
    }

    // check Request Runtime Permissions enabled or not for Google Map
    protected boolean isPermissionEnabled(String... permissions) {
        boolean flag = false;

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != 0) {
                flag = false;
                break;
            }

            flag = true;
        }

        return flag;
    }

    protected void showDenialDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false);
        builder.setTitle("Permission Required!");
        builder.setMessage("Permissions are required to proceed. ");
        builder.setPositiveButton("Goto Settings", (dialog, which) -> {
            dialog.dismiss();
            gotoSettings();
        });
        builder.setNegativeButton("Dismiss", (dialog, which) -> dialog.dismiss());
        builder.create().show();
    }

    // Goto Settings to enable Permissions
    protected void gotoSettings() {
        Intent intent = new Intent();
        intent.setAction("android.settings.APPLICATION_DETAILS_SETTINGS");
        Uri uri = Uri.fromParts("package", getPackageName(), (String)null);
        intent.setData(uri);
        startActivity(intent);
    }

    // Change Google map's style at runtime
    protected int findGoogleMapStyle(int id){

        int mapType = GoogleMap.MAP_TYPE_NORMAL;

        switch (id){
            case GoogleMap.MAP_TYPE_NONE:{
                mapType = GoogleMap.MAP_TYPE_NONE;
                break;
            }
            case GoogleMap.MAP_TYPE_SATELLITE:{
                mapType = GoogleMap.MAP_TYPE_SATELLITE;
                break;
            }
            case GoogleMap.MAP_TYPE_TERRAIN:{
                mapType = GoogleMap.MAP_TYPE_TERRAIN;
                break;
            }
            case GoogleMap.MAP_TYPE_HYBRID:{
                mapType = GoogleMap.MAP_TYPE_HYBRID;
                break;
            }
            default:
                break;
        }

        return mapType;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){
            if(requestCode == GPS_REQUEST_CODE){
                if(!isGPSEnabled())
                    showGPSDialogBox();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.length > 0) {
            boolean flag = false;

            for (int result : grantResults) {
                if (result != 0) {
                    flag = false;
                    break;
                }

                flag = true;
            }

            if (flag) {
                onGranted();
            } else {
                onDenied();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "_onMapReady: ");
        onGoogleMapReady(googleMap);
    }
}
