package com.example.shashi.thedementorsnetwork;

import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.content.SharedPreferences;
import android.util.Log;
import java.util.*;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

//import com.google.firebase.database.DatabaseReference;
import com.google.firebase.auth.*;
import com.google.firebase.database.*;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;

class Userr {

    public String username;
    public double lati;
    public double longi;

    public Userr() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public Userr(String username, double lati, double longi) {
        this.username = username;
        this.lati = lati;
        this.longi = longi;
    }

}


public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, ConnectionCallbacks, OnConnectionFailedListener, LocationListener {

    //google maps----------------------------------------------------------
    private static final String TAG = "Map Activity: " ;
    private GoogleMap mMap;
    protected GoogleApiClient mGoogleApiClient;
    protected Location mCurrentLocation;
    private double lati;
    private double longi;
    private Marker[] mMypos = new Marker[100];
    private Marker mSydney;
    protected LocationRequest mLocationRequest;
    protected Boolean mRequestingLocationUpdates;
    Integer[] buhu = new Integer[100];



    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 2000;


    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;
    //google maps----------------------------------------------------------


    //firebase things--------------------------------------------------------------------
    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    Integer userId, userNo=0;
    //firebase things--------------------------------------------------------------------


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
            createLocationRequest();



        }
        Integer i=0;
        for (i = 1; i <= 20; i++) {
            buhu[i] = 0;
        }

        //------------------------------------------------------------------------------------------------
        //firebase part ----------------------------------------------------------------------------------
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();
        userId = pref.getInt("key_name", -1);




        mDatabase.child("userNo").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userNo = dataSnapshot.getValue(Integer.class);
                if (userId>userNo){
                    userId=-1;
                    editor.putInt("key_name", userId);
                    editor.commit();
                }
                Log.d(TAG,"Your user Id: " + userId +"\n"+"No of users is: "+userNo);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d(TAG, "Failed to read value.");
            }
        });

        mDatabase.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
//                textDataPrint.setText("");

                if(userNo!=0) {
                    Integer mark = 1;



                    for (mark = 1; mark <= userNo; mark++) {
                        Userr userp = dataSnapshot.child(Integer.toString(mark)).getValue(Userr.class);
                        Log.d(TAG,"from datachange: "+ "lati: "+userp.lati+"longi: "+userp.longi+" userno: "+mark);
                        LatLng mypos = new LatLng(userp.lati, userp.longi);
//                        mMap.addMarker(new MarkerOptions().position(mypos).title("Marker in mypos"));
                        if (buhu[mark] != 0) {
                            mMypos[mark].remove();
                        }
                        buhu[mark] = 1;
                        mMypos[mark] = mMap.addMarker(new MarkerOptions().position(mypos).title(userp.username));
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.d(TAG, "Failed to read value.");
            }
        });

        //firebase part ----------------------------------------------------------------------------------
        //------------------------------------------------------------------------------------------------

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        // The final argument to {@code requestLocationUpdates()} is a LocationListener
        // (http://developer.android.com/reference/com/google/android/gms/location/LocationListener.html).
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    private void updateUI() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences("MyPref", MODE_PRIVATE);
        final SharedPreferences.Editor editor = pref.edit();

//        EditText eText = (EditText) findViewById(R.id.userName);
//        String userName = eText.getText().toString();

        if (mCurrentLocation != null) {
            lati = mCurrentLocation.getLatitude();
            longi = mCurrentLocation.getLongitude();
//            LatLng mypos = new LatLng(lati, longi);
            Log.d(TAG, "From upload: " + longi + "   " + lati);

            User user = new User(pref.getString("user_name", " "), lati, longi);

            database.getReference().child("users").child(Integer.toString(userId)).setValue(user);
//            if(buhu!=0){
//                mMypos.remove();
//            }
//            buhu=1;
//            mMypos = mMap.addMarker(new MarkerOptions().position(mypos).title("Marker in mypos"));
        }

    }

    @Override
    public void onConnected(Bundle connectionHint) {

        if (mCurrentLocation == null) {
            mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            updateUI();
        }
        startLocationUpdates();

    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {

        Log.i(TAG, "Connection failed: ConnectionResult.getErrorCode() = " + result.getErrorCode());
    }


    @Override
    public void onConnectionSuspended(int cause) {

        Log.i(TAG, "Connection suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        updateUI();
    }


    protected void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
        }

        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mSydney = mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
    }
}
