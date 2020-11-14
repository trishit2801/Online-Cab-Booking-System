package com.example.rapidcabs;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

public class ViewRequestsActivity extends AppCompatActivity {

    ArrayList<String> requests = new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;
    ListView requestsListView;
    LocationManager locationManager;
    LocationListener locationListener;
    String rideRequestedBy;
    ArrayList<Double> requestLatitude = new ArrayList<Double>();
    ArrayList<Double> requestLongitude = new ArrayList<Double>();
    ArrayList<String> usernames = new ArrayList<String>();
    public void updateListView(Location location){
        if(location != null){

            ParseQuery<ParseObject> query = ParseQuery.getQuery("Request");
            final ParseGeoPoint geoPointLocation = new ParseGeoPoint(location.getLatitude(),location.getLongitude());
            query.whereNear("location", geoPointLocation);
            query.whereDoesNotExist("driverName");
            query.setLimit(10);
            query.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        requests.clear();
                        requestLongitude.clear();
                        requestLatitude.clear();
                        if(objects.size()>0){
                            for(ParseObject object : objects){
                                ParseGeoPoint requestLocation = (ParseGeoPoint)object.get("location");
                                if(requestLocation != null) {

                                    Double distanceInKilometeres = geoPointLocation.distanceInKilometersTo(requestLocation);
                                    Double distanceInOneDP = (double) Math.round(distanceInKilometeres * 10) / 10;
                                    /*
                                      void merge(int arr[], int beg, int mid, int end)
                                      {

                                         int l = mid - beg + 1;
                                         int r = end - mid;

                                         intLeftArray[] = new int [l];
                                         intRightArray[] = new int [r];

                                         for (int i=0; i<l; ++i)
                                            LeftArray[i] = arr[beg + i];

                                         for (int j=0; j<r; ++j)
                                            RightArray[j] = arr[mid + 1+ j];


                                         int i = 0, j = 0;
                                         int k = beg;
                                         while (i<l&&j<r)
                                         {
                                            if (LeftArray[i] <= RightArray[j])
                                            {
                                                arr[k] = LeftArray[i];
                                                i++;
                                             }
                                            else
                                            {
                                                arr[k] = RightArray[j];
                                                j++;
                                             }
                                            k++;
                                          }
                                          while (i<l)
                                          {
                                               arr[k] = LeftArray[i];
                                               i++;
                                               k++;
                                           }

                                          while (j<r)
                                          {
                                              arr[k] = RightArray[j];
                                              j++;
                                              k++;
                                           }
                                        }

                                        void sort(int arr[], int beg, int end)
                                        {
                                          if (beg<end)
                                            {
                                                int mid = (beg+end)/2;
                                                sort(arr, beg, mid);
                                                sort(arr , mid+1, end);
                                                merge(arr, beg, mid, end);
                                             }
                                         }
                                     */
                                    requests.add("Pick up at " + distanceInOneDP.toString() + " kms");
                                    requestLatitude.add(requestLocation.getLatitude());
                                    requestLongitude.add(requestLocation.getLongitude());
                                    usernames.add(object.getString("username"));
                                }
                            }
                            requestsListView.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();
                        }
                        else{
                            requests.add("No active requests nearby");
                            requestsListView.setAdapter(arrayAdapter);
                            arrayAdapter.notifyDataSetChanged();
                        }
                    }
                    else{
                        e.printStackTrace();
                    }

                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 1){
            if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED){
                if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,locationListener);
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    updateListView(lastKnownLocation);
                }
            }
        }
    }
    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_requests);
        setTitle("Nearby Requests");

        requestsListView = (ListView)findViewById(R.id.requestsListView);
        requests.clear();
        requests.add("Getting nearby requests....");
        arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,requests);
        requestsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (Build.VERSION.SDK_INT < 23 || ContextCompat.checkSelfPermission(ViewRequestsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if(requestLatitude.size() > position && requestLongitude.size() > position && usernames.size() > position && lastKnownLocation!=null){

                        Intent intent = new Intent(getApplicationContext(),DriverLocationActivity.class);

                        intent.putExtra("requestLatitude",requestLatitude.get(position));
                        intent.putExtra("requestLongitude", requestLongitude.get(position));
                        intent.putExtra("driverLatitude", lastKnownLocation.getLatitude());
                        intent.putExtra("driverLongitude", lastKnownLocation.getLongitude());
                        intent.putExtra("username", usernames.get(position));

                        startActivity(intent);
                    }

                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                updateListView(location);
                ParseUser.getCurrentUser().put("location", new ParseGeoPoint(location.getLatitude(),location.getLongitude()));
                ParseUser.getCurrentUser().saveInBackground();

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
                    updateListView(lastKnownLocation);
                }
            }
        }
    }
}