package com.example.ne.foodneed;

import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HotelAddLocation extends AppCompatActivity implements OnMapReadyCallback,LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private Button done;
    private static Marker currentLocationMarker=null;
    private static final long INTERVAL = 500;
    private static int currentloc = 0;
    private static final long FASTEST_INTERVAL = 300;
    private GoogleMap mMap;
    private double hlat,hlon;
    private SharedPreferences sharedPreferences;
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    List<Address> addresses = new ArrayList<>();
    Geocoder geocoder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_add_location);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapHotelAddLocation);
        mapFragment.getMapAsync(this);

        geocoder = new Geocoder(HotelAddLocation.this, Locale.getDefault());

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(20000);
        //mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);

        hlat = Double.parseDouble(sharedPreferences.getString("lat","0"));
        hlon = Double.parseDouble(sharedPreferences.getString("lon","0"));

        done = (Button) findViewById(R.id.mapDone);

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        findViewById(R.id.btnAddLocationGPS).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentloc = 1;
                setCurrentLocation();
            }
        });
    }

    private void addMarker(GoogleMap googleMap, LatLng latLng) {

        MarkerOptions markerOptions = new MarkerOptions();

        // Setting the position for the marker
        markerOptions.position(latLng);

        // Setting the title for the marker.
        // This will be displayed on taping the marker
        markerOptions.title(latLng.latitude + " : " + latLng.longitude);
        try {
            UserData.lat = "" + latLng.latitude;
            UserData.lon = "" + latLng.longitude;
        }catch(Exception e){

        }
        // Clears the previously touched position
        googleMap.clear();

        // Animating to the touched position
        googleMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));

        // Placing a marker on the touched position
        googleMap.addMarker(markerOptions);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mGoogleApiClient.connect();

        LatLng latLng = new LatLng(hlat,hlon);
        addMarker(mMap,latLng);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                currentloc = 0;
                mMap.clear();
                addMarker(mMap, latLng);
                UserData.lat = "" + latLng.latitude;
                UserData.lon = "" + latLng.longitude;
                UserData.address = getAddress();
            }
        });
    }

    private String getAddress(){
        List<Address> addresses = new ArrayList<>();
        String knownName = "",address="none";
        try {
            addresses = geocoder.getFromLocation(Double.parseDouble(UserData.lat), Double.parseDouble(UserData.lon), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            knownName = addresses.get(0).getFeatureName();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return address;
    }

    public void setCurrentLocation(){
        try {
            if(currentloc==1) {
                mMap.clear();
                if(!(currentLocationMarker==null)){
                    currentLocationMarker.remove();
                }
                LatLng sydney = new LatLng(Double.parseDouble(UserData.lat), Double.parseDouble(UserData.lon));
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Your current location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(sydney)
                        .zoom(18)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                UserData.address = getAddress();
                UserData.sellat = UserData.lat;
                UserData.sellon = UserData.lon;
                UserData.address = getAddress();
            }
        }catch(Exception e){}
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HotelAddLocation.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, HotelAddLocation.this);
        }catch(Exception e){}
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        try{
            mGoogleApiClient.connect();
        }catch(Exception e){
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        UserData.lat = ""+location.getLatitude();
        UserData.lon = ""+location.getLongitude();
        setCurrentLocation();
    }
}
