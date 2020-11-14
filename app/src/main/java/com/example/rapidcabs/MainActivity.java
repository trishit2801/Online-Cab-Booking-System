package com.example.rapidcabs;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    Switch userTypeSwitch;
    String userType;
    public void redirectUser(){
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", ParseUser.getCurrentUser().getUsername());
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null && objects.size()>0){
                    for(ParseUser user:objects){
                        String currentUserType = user.getString("riderOrDriver");
                        if(currentUserType.equals("rider")){
                            Intent intent = new Intent(getApplicationContext(),RiderLoginActivity.class);
                            startActivity(intent);
                        }
                        else if(currentUserType.equals("driver")){
                            Intent intent = new Intent(getApplicationContext(),DriverLoginActivity.class);
                            startActivity(intent);
                        }
                    }
                }
            }
        });

    }
    public void getStarted(View view){

        Log.i("Switch value", String.valueOf(userTypeSwitch.isChecked()));
        userType = "rider";
        if(userTypeSwitch.isChecked()){
            userType = "driver";
        }

        Log.i("Redirecting as:", userType);
        if(userType.equals("rider")){
            Intent intent1 = new Intent(getApplicationContext(),RiderLoginActivity.class);
            intent1.putExtra("userType", userType);
            startActivity(intent1);
        }
        else if(userType.equals("driver")){
            Intent intent1 = new Intent(getApplicationContext(),DriverLoginActivity.class);
            intent1.putExtra("userType", userType);
            startActivity(intent1);
        }
        /*ParseUser.getCurrentUser().put("riderOrDriver", userType);
        ParseUser.getCurrentUser().saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null){
                    redirectUser();
                }
                else{
                    e.printStackTrace();
                }
            }
        }); */

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        userTypeSwitch = (Switch)findViewById(R.id.userTypeSwitch);
        if(ParseUser.getCurrentUser() != null) {
            if (ParseUser.getCurrentUser().getString("riderOrDriver") != null) {
                redirectUser();
            }
        }
    }
}