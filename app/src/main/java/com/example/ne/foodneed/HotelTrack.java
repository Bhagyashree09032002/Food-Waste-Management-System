package com.example.ne.foodneed;

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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

public class HotelTrack extends AppCompatActivity implements OnMapReadyCallback, LocationListener, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    Button donate;
    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    private double flat = 0.0, flon = 0.0, ngolat = 0.0, ngolon = 0.0;
    private static Marker foodmarker = null,ngomarker=null;
    private static int did=0;
    static List<LatLng> directionList;
    public static final int MY_SOCKET_TIMEOUT_MS = 5000;
    private static int currentStep = 0,count=0;
    private StepView stepView;
    private static String status="",ngoname="";
    private List<String> steps = new ArrayList<>();
    Button homepage,editprofile;

    private String sharedPrefFile = "com.example.source.foodneedsharedprefs";
    private String trackFoodURL = IPaddress.ip + "trackfoodhotel.php";
    private BitmapDescriptor foodicon;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hotel_track);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.mapTrack);
        mapFragment.getMapAsync(this);

        donate = (Button) findViewById(R.id.btnHotelDonateFood);

        stepView = (StepView) findViewById(R.id.stepViewBar);
        steps.add("Allocated to NGO");
        steps.add("On the way to destination");
        steps.add("Delivered to destination");
        stepView.setSteps(steps);

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

        findViewById(R.id.textFoodStatus).setVisibility(View.INVISIBLE);

        donate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HotelTrack.this,HotelHomepage.class);
                startActivity(i);
                overridePendingTransition(R.anim.fade_in,R.anim.fade_out);
                finish();
            }
        });
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(HotelTrack.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Toast.makeText(this, "Something went wrong!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        try {
            mGoogleApiClient.connect();
        } catch (Exception e) {
            Toast.makeText(this, "Error: " + e.getStackTrace(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        UserData.lat = "" + location.getLatitude();
        UserData.lon = "" + location.getLongitude();

        getLocationUpdatesOfFood();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        foodicon = BitmapDescriptorFactory.fromResource(R.drawable.small1food1);
        mGoogleApiClient.connect();
    }

    private void getLocationUpdatesOfFood() {
        RequestParams params = new RequestParams();
        params.put("uid", sharedPreferences.getString("id", "0"));
        params.put("usertype", sharedPreferences.getString("usertype", "none"));

        //final ProgressDialog pDialog = ProgressDialog.show(Track.this,"Processing","please wait...",true,false);

        AsyncHttpClient client = new AsyncHttpClient();
        client.get(trackFoodURL, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                //pDialog.dismiss();
                String res = new String(responseBody);
                try {
                    JSONObject o = new JSONObject(res);
                    did = o.getInt("donationid");
                    if(did!=0) {
                        flat = o.getDouble("flat");
                        flon = o.getDouble("flon");
                        ngolat = o.getDouble("ngolat");
                        ngolon = o.getDouble("ngolon");
                        status = o.getString("status");
                        ngoname = o.getString("ngoname");
                        stepView.setSteps(steps);
                        if (status.equals("Allocated")) {
                            stepView.go(1, true);
                            stepView.done(true);
                        }
                        if (status.equals("on the way")) {
                            stepView.go(2, true);
                            stepView.done(true);
                        }
                        if (status.equals("Delivered")) {
                            stepView.go(3, true);
                            stepView.done(true);
                        }
                        if(foodmarker!=null){
                            foodmarker.remove();
                        }
                        if(ngomarker!=null){
                            ngomarker.remove();
                        }
                        MarkerOptions markerOptions = new MarkerOptions();
                        LatLng latLng = new LatLng(flat, flon);
                        markerOptions.position(latLng)
                                .icon(foodicon)
                                .title("Food location");
                        foodmarker = mMap.addMarker(markerOptions);
                        MarkerOptions markerOptions1 = new MarkerOptions();
                        LatLng latLng1 = new LatLng(ngolat, ngolon);
                        markerOptions1.position(latLng1)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN))
                                .title("NGO location");
                        ngomarker = mMap.addMarker(markerOptions1);
                        CameraPosition cameraPosition = new CameraPosition.Builder()
                                .target(latLng)
                                .zoom(18)
                                .build();
                        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                        String directionApiPath = "https://maps.googleapis.com/maps/api/directions/json?origin=" + String.valueOf(flat) + "," + String.valueOf(flon) + "&destination=" + String.valueOf(ngolat) + "," + String.valueOf(ngolon);

                        getDirectionFromDirectionApiServer(directionApiPath);
                    }else{
                        findViewById(R.id.textFoodStatus).setVisibility(View.VISIBLE);
                        stepView.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Toast.makeText(HotelTrack.this, res, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                //pDialog.dismiss();
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
                        Toast.makeText(HotelTrack.this, "Maps server error!", Toast.LENGTH_SHORT).show();
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

    /**
     * Method to decode polyline points
     * Courtesy : http://jeffreysambells.com/2010/05/27/decoding-polylines-from-google-maps-direction-api-with-java
     * */
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
    protected void onDestroy() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, HotelTrack.this);
        } catch (Exception e) {
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        try {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, HotelTrack.this);
        } catch (Exception e) {
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
                return;
            }
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, HotelTrack.this);
        }catch(Exception e){}
        super.onResume();
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

    @Override
    public void onBackPressed() {
        count ++;
        if(count==1) {
            final CountDownTimer t = new CountDownTimer(1000,100){
                @Override
                public void onTick(long millisUntilFinished) {
                    if(count>1){
                        onFinish();
                    }
                }

                @Override
                public void onFinish() {
                    if(count>1) {
                        UserData.name = "";
                        SharedPreferences sharedPreferences = getSharedPreferences(sharedPrefFile, MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.clear();
                        editor.commit();
                        UserData.id = "";
                        UserData.fid = "0";
                        Intent i = new Intent(HotelTrack.this, Login.class);
                        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        startActivity(i);
                        finish();
                    }else{
                        count=0;
                        Toast.makeText(HotelTrack.this, "Continuously press back button two times to logout!", Toast.LENGTH_SHORT).show();
                    }
                }
            }.start();
        }
    }
}
