package com.pakistan.jkgooglemap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.pakistan.jkgooglymap.JKGoogleMapActivity;

public class MainActivity extends JKGoogleMapActivity {

    private static final String TAG = "MainActivity";

    private GoogleMap mGoogleMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initMap();
        initGooglePlaces(R.id.google_map_auto_complete_places_search_fragment);
    }

    // initialize Map
    private void initMap() {
        if (hasPlayServices()) {
            if (isPermissionEnabled(PERMISSIONS)) {
                initGoogleMap(R.id.map);
            } else
                requestRuntimePermissions();
        }
    }

    // initialize Current Location
    private void initCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        mGoogleMap.setMyLocationEnabled(true);
        mGoogleMap.getUiSettings().setMyLocationButtonEnabled(false);
        findCurrentDeviceLocation();
    }

    @Override
    public void onGranted() {
        Log.d(TAG, "_on_Permissions_Granted: ");
        Toast.makeText(this, "Permissions Granted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDenied() {
        Log.d(TAG, "_on_Permissions_Denied: ");
        showDenialDialog();
    }

    @Override
    public void onGoogleMapReady(GoogleMap map) {
        Log.d(TAG, "_onGoogleMapReady: ");
        Toast.makeText(this, "Map is Ready", Toast.LENGTH_SHORT).show();
        mGoogleMap = map;
        initCurrentLocation();
    }

    @Override
    protected void onCurrentLocationFound(Location currentLocation, String msg) {
        Log.d(TAG, "_onCurrentLocationFound: "+currentLocation);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        animateCameraTo(mGoogleMap, new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()));
    }

    @Override
    protected void onPlaceSearched(Place place, String msg) {
        Log.d(TAG, "_onPlaceSearched: "+place);
        Log.d(TAG, "_onPlaceSearched_Msg: "+msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();

        if(place != null){
            addMarkerTo(mGoogleMap, place.getLatLng(), place.getName().toString(),place.getLocale().toString(), R.drawable.ic_target_location);
            animateCameraTo(mGoogleMap, place.getLatLng());
        }

    }

}