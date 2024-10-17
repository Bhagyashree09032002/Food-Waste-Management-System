package com.example.ne.foodneed;

import android.*;
import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class NavigateActivity extends FragmentActivity implements OnMapReadyCallback,LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private SharedPreferences sharedPreferences;
    private static double flat=0.0,flon=0.0;
    private static Marker foodmarker = null;
    private static BitmapDescriptor foodicon;
    private static String status="0",did="0";
    static List<LatLng> directionList;
    public static final int MY_SOCKET_TIMEOUT_MS = 5000;
    private static int count=0;
    private int currentloc = 1;
    private Marker currentLocationMarker=null;
    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    private String updateFoodLocationURL = IPaddress.ip+"updatefoodloc.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigate);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sharedPreferences = getSharedPreferences(sharedPrefFile,MODE_PRIVATE);
        flat = Double.parseDouble(sharedPreferences.getString("flat","0"));
        flon = Double.parseDouble(sharedPreferences.getString("flon","0"));
        status = sharedPreferences.getString("status","0");
        did = sharedPreferences.getString("did","0");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(10000);
        //mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        foodicon = BitmapDescriptorFactory.fromResource(R.drawable.small1food1);
        LatLng latLng = new LatLng(flat,flon);
        MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.title("Food location")
                .icon(foodicon)
                .position(latLng);
        foodmarker = mMap.addMarker(markerOptions);
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NavigateActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},1);
        }else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, NavigateActivity.this);
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
        mMap.clear();
        UserData.lat = ""+location.getLatitude();
        UserData.lon = ""+location.getLongitude();

        setCurrentLocation();
        if(status.equals("On the way")){
            updateFoodLoc();
        }
    }

    public void setCurrentLocation(){
        try {
            if(currentloc==1) {
                if(!(currentLocationMarker==null)){
                    currentLocationMarker.remove();
                }
                if(foodmarker!=null){
                    foodmarker.remove();
                }
                LatLng latLng = new LatLng(flat,flon);
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.title("Food location")
                        .icon(foodicon)
                        .position(latLng);
                foodmarker = mMap.addMarker(markerOptions);
                LatLng sydney = new LatLng(Double.parseDouble(UserData.lat), Double.parseDouble(UserData.lon));
                currentLocationMarker = mMap.addMarker(new MarkerOptions().position(sydney).title("Your current location"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(sydney)
                        .zoom(18)
                        .build();
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                String directionApiPath = "https://maps.googleapis.com/maps/api/directions/json?origin=" + String.valueOf(UserData.lat) + "," + String.valueOf(UserData.lon) + "&destination=" + flat + "," + flon;

                getDirectionFromDirectionApiServer(directionApiPath);
            }
        }catch(Exception e){}
    }

    private void updateFoodLoc(){
        RequestParams params = new RequestParams();
        params.put("did",did);
        params.put("flat",UserData.lat);
        params.put("flon",UserData.lon);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(updateFoodLocationURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                String res = new String(responseBody);
                if(res.equals("200")){

                }else{
                    Toast.makeText(NavigateActivity.this, "Location update failed!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Toast.makeText(NavigateActivity.this, "Location update failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDirectionFromDirectionApiServer(String url) {
        GsonRequest<DirectionObject> serverRequest = new GsonRequest<DirectionObject>(
                Request.Method.GET,
                url,
                DirectionObject.class,
                createRequestSuccessListener(),
                createRequestErrorListener());
        serverRequest.setRetryPolicy(new DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(serverRequest);
    }

    private Response.Listener<DirectionObject> createRequestSuccessListener() {
        return new Response.Listener<DirectionObject>() {
            @Override
            public void onResponse(DirectionObject response) {
                try {
                    Log.d("JSON Response", response.toString());
                    if (response.getStatus().equals("OK")) {
                        List<LatLng> mDirections = getDirectionPolylines(response.getRoutes());
                        drawRouteOnMap(mMap, mDirections);
                    } else {
                        Toast.makeText(NavigateActivity.this, "Maps server error!", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ;
        };
    }

    private List<LatLng> getDirectionPolylines(List<RouteObject> routes) {
        directionList = new ArrayList<LatLng>();
        for (RouteObject route : routes) {
            List<LegsObject> legs = route.getLegs();
            for (LegsObject leg : legs) {
                List<StepsObject> steps = leg.getSteps();
                for (StepsObject step : steps) {
                    PolylineObject polyline = step.getPolyline();
                    String points = polyline.getPoints();
                    List<LatLng> singlePolyline = decodePoly(points);
                    for (LatLng direction : singlePolyline) {
                        directionList.add(direction);
                    }
                }
            }
        }
        return directionList;
    }

    private Response.ErrorListener createRequestErrorListener() {
        return new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
    }

    private void drawRouteOnMap(GoogleMap map, List<LatLng> positions) {
        PolylineOptions options = new PolylineOptions().width(8).color(Color.BLUE).geodesic(true);
        options.addAll(positions);
        Polyline polyline = map.addPolyline(options);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(positions.get(1).latitude, positions.get(1).longitude))
                .zoom(18)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }


    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;
            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode){
            case 1 : {
                if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    mGoogleApiClient.connect();
                }else{
                    ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                }
            }
        }
    }

}
