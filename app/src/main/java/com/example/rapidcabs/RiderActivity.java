package com.example.rapidcabs;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.List;

public class RiderActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    LocationManager locationManager;
    LocationListener locationListener;
    Button bookRideButton;
    Button liveChatButton;
    Button voiceChatButton;
    EditText destinationEditText;
    boolean requestIsActive = false;
    TextView infoTextView;
    Handler handler = new Handler();
    boolean driverActive =   false;
    public void voiceChat(View view){
      Intent intent = new Intent(getApplicationContext(),VoiceChatActivity.class);
      intent.putExtra("rider",ParseUser.getCurrentUser().getUsername());
      startActivity(intent);
    }
    public void liveChat(View view){
      Intent intent = new Intent(getApplicationContext(),LiveChatActivity.class);
      intent.putExtra("rider",ParseUser.getCurrentUser().getUsername());
      startActivity(intent);
    }
    public void checkForUpdates(){

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.whereExists("driverName");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    driverActive = true;
                    ParseQuery<ParseUser> query1 = ParseUser.getQuery();
                    query1.whereEqualTo("username", objects.get(0).getString("driverName"));
                    query1.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> objects, ParseException e) {
                            if (e == null && objects.size() > 0) {

                                ParseGeoPoint driverLocation = objects.get(0).getParseGeoPoint("location");
                                if (Build.VERSION.SDK_INT < 23|| ContextCompat.checkSelfPermission(RiderActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                    if(lastKnownLocation!=null) {

                                        ParseGeoPoint riderLocation = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                                        Double distanceInKilometeres = driverLocation.distanceInKilometersTo(riderLocation);

                                        if(distanceInKilometeres < 0.1){

                                            Toast.makeText(RiderActivity.this, "Meet your driver at the pickup point", Toast.LENGTH_LONG).show();
                                            infoTextView.setText("Your driver has arrived");
                                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Request");
                                            query2.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

                                            query2.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if(e == null){

                                                        for(ParseObject object:objects){
                                                            object.deleteInBackground();
                                                        }
                                                    }
                                                }
                                            });

                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    infoTextView.setText("");
                                                    bookRideButton.setVisibility(View.VISIBLE);
                                                    liveChatButton.setVisibility(View.INVISIBLE);
                                                    voiceChatButton.setVisibility(View.INVISIBLE);
                                                    destinationEditText.setVisibility(View.VISIBLE);
                                                    destinationEditText.setText("");
                                                    bookRideButton.setText("Book a Ride");
                                                    requestIsActive = false;
                                                    driverActive = false;

                                                }
                                            },8000);
                                        }
                                        /*if (distanceInKilometeres < 0.1) {

                                            Toast.makeText(RiderActivity.this, "Meet your driver at the pickup point", Toast.LENGTH_LONG).show();
                                            infoTextView.setText("Your driver has arrived");

                                            ParseQuery<ParseObject> query2 = ParseQuery.getQuery("Request");
                                            query2.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());

                                            query2.findInBackground(new FindCallback<ParseObject>() {
                                                @Override
                                                public void done(List<ParseObject> objects, ParseException e) {
                                                    if(e == null){

                                                        for(ParseObject object:objects){
                                                            object.deleteInBackground();
                                                        }
                                                    }
                                                }
                                            });
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {

                                                    infoTextView.setText("");
                                                    bookRideButton.setVisibility(View.VISIBLE);
                                                    bookRideButton.setText("Book a Ride");
                                                    requestIsActive = false;
                                                    driverActive = false;

                                                }
                                            },8000);

                                        } */
                                        else {
                                            Double distanceInOneDP = (double) Math.round(distanceInKilometeres * 10) / 10;
                                            infoTextView.setText("Your driver is " + distanceInOneDP + " kms away");

                                            LatLng driverLocationLatLng = new LatLng(driverLocation.getLatitude(), driverLocation.getLongitude());
                                            LatLng riderLocationLatLng = new LatLng(riderLocation.getLatitude(), riderLocation.getLongitude());

                                            ArrayList<Marker> markers = new ArrayList<>();
                                            mMap.clear();
                                            markers.add(mMap.addMarker(new MarkerOptions().position(driverLocationLatLng).title("Your driver is on the way").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))));
                                            markers.add(mMap.addMarker(new MarkerOptions().position(riderLocationLatLng).title("Your location")));

                                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                            for (Marker marker : markers) {
                                                builder.include(marker.getPosition());
                                            }
                                            LatLngBounds bounds = builder.build();

                                            int padding = 30;
                                            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, padding);
                                            mMap.animateCamera(cu);

                                            bookRideButton.setVisibility(View.INVISIBLE);
                                            destinationEditText.setVisibility(View.INVISIBLE);
                                            voiceChatButton.setVisibility(View.VISIBLE);
                                            liveChatButton.setVisibility(View.VISIBLE);

                                        }
                                    }

                                }
                            }
                        }
                    });

                }
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        checkForUpdates();
                    }
                },2000);
            }

        });
    }
    public void logout(View view){
        ParseUser.logOut();
        Toast.makeText(RiderActivity.this,"You have successfully logged out!",Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(getApplicationContext(),RiderLoginActivity.class);
        startActivity(intent);
    }
    public void bookRide(View view) {
        if(requestIsActive == true){
            // CANCEL A RIDE
            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null && objects.size()>0){
                        for(ParseObject object:objects){
                            object.deleteInBackground(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){
                                        Toast.makeText(RiderActivity.this, "Your ride has been cancelled", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                        requestIsActive = false;
                        bookRideButton.setText("Book a Ride");
                        liveChatButton.setVisibility(View.INVISIBLE);
                        voiceChatButton.setVisibility(View.INVISIBLE);
                        destinationEditText.setText("");
                    }
                }
            });
        }
        else {
            // BOOK A RIDE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null && (!destinationEditText.getText().toString().equals(""))) {
                    ParseObject request = new ParseObject("Request");
                    request.put("username", ParseUser.getCurrentUser().getUsername());
                    ParseGeoPoint parseGeoPoint = new ParseGeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                    request.put("location", parseGeoPoint);
                    request.put("destination", destinationEditText.getText().toString());
                    request.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e == null) {
                                Toast.makeText(RiderActivity.this, "Request for Ride has been sent", Toast.LENGTH_SHORT).show();
                                bookRideButton.setText("Cancel Ride");
                                destinationEditText.setVisibility(View.INVISIBLE);
                                liveChatButton.setVisibility(View.VISIBLE);
                                voiceChatButton.setVisibility(View.VISIBLE);
                                requestIsActive = true;

                                checkForUpdates();
                            }
                            else {
                                e.printStackTrace();
                            }
                        }
                    });
                }
                else if(destinationEditText.getText().toString().equals("")){
                    Toast.makeText(this, "Please specify drop off location!", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(RiderActivity.this,"We could not find your location.Try Again later!",Toast.LENGTH_SHORT).show();
                }

            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length>0 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateMap(lastKnownLocation);
                }
            }
        }
    }

    public void updateMap(Location location) {
        if(driverActive == false) {
            LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            mMap.addMarker(new MarkerOptions().position(userLocation).title("You're here"));
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rider);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        bookRideButton = (Button)findViewById(R.id.bookRideButton);
        infoTextView = (TextView)findViewById(R.id.infoTextView);
        destinationEditText = (EditText)findViewById(R.id.destinationEditText);
        liveChatButton = (Button)findViewById(R.id.liveChatWithRiderButton);
        voiceChatButton = (Button)findViewById(R.id.voiceChatButton);

        liveChatButton.setVisibility(View.INVISIBLE);
        voiceChatButton.setVisibility(View.INVISIBLE);

        ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
        query.whereEqualTo("username",ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    requestIsActive = true;
                    bookRideButton.setText("Cancel Ride");
                    checkForUpdates();
                }
            }
        });
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateMap(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        if (Build.VERSION.SDK_INT < 23) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }
        else{
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION},1);
            }
            else{
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if(lastKnownLocation!=null){
                    updateMap(lastKnownLocation);
                }
            }
        }
    }
}