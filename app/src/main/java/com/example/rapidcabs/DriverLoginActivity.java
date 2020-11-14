package com.example.rapidcabs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnalytics;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;

public class DriverLoginActivity extends AppCompatActivity implements View.OnClickListener, View.OnKeyListener {

    EditText usernameEditText;
    EditText passwordEditText;
    TextView loginTextView;
    Button signupButton;
    boolean signupModeActive = true;

    public void redirectUser(){
        if(ParseUser.getCurrentUser()!=null){
            Intent intent = new Intent(getApplicationContext(),ViewRequestsActivity.class);
            startActivity(intent);
        }
    }
    @Override
    public boolean onKey(View view, int i, KeyEvent keyEvent) {
        if(i == KeyEvent.KEYCODE_ENTER && keyEvent.getAction()==KeyEvent.ACTION_DOWN){
            signUp(view);
        }
        return false;
    }
    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.loginTextView){
            if(signupModeActive){
                signupModeActive = false;
                signupButton.setText("LOGIN AS DRIVER");
                loginTextView.setText("Not having an account? Create One");
            }
            else{
                signupModeActive = true;
                signupButton.setText("SIGN UP AS DRIVER");
                loginTextView.setText("Already have an account? LOGIN");
            }
        }
        else if(view.getId() == R.id.cabsImageView || view.getId() == R.id.backgroundLayout){
            InputMethodManager inputMethodManager = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
        }
    }

    public void signUp(View view) {
        if (usernameEditText.getText().toString().matches("") || passwordEditText.getText().toString().matches("")) {
            Toast.makeText(DriverLoginActivity.this, "A username and a password is required", Toast.LENGTH_SHORT).show();
        }
        else {
            if (signupModeActive) {
                ParseUser user = new ParseUser();  // Signing up new user
                user.setUsername(usernameEditText.getText().toString());
                user.setPassword(passwordEditText.getText().toString());
                user.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            Toast.makeText(getApplicationContext(), "You are successfully signed up", Toast.LENGTH_SHORT).show();
                            Intent intent1 = getIntent();
                            String type = intent1.getStringExtra("userType");
                            if(type!=null) {
                                Log.i("info", type);
                                ParseUser.getCurrentUser().put("riderOrDriver", type);
                                ParseUser.getCurrentUser().saveInBackground();
                                redirectUser();
                            }
                        }
                        else {
                            Toast.makeText(DriverLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
            else{
                ParseUser.logInInBackground(usernameEditText.getText().toString(), passwordEditText.getText().toString(), new LogInCallback() {   // logging in existing user
                    @Override
                    public void done(ParseUser user, ParseException e) {
                        if(e == null && user!=null){
                            Toast.makeText(DriverLoginActivity.this,"Logged in successfully", Toast.LENGTH_SHORT).show();
                            redirectUser();
                        }
                        else{
                            Toast.makeText(DriverLoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_login);

        getSupportActionBar().hide();

        usernameEditText = (EditText)findViewById(R.id.usernameEditText);
        passwordEditText = (EditText)findViewById(R.id.passwordEditText);
        signupButton = (Button)findViewById(R.id.signupButton);
        loginTextView = (TextView)findViewById(R.id.loginTextView);
        loginTextView.setOnClickListener(this);
        passwordEditText.setOnKeyListener(this);
        ImageView uberImageView  = (ImageView)findViewById(R.id.cabsImageView);
        ConstraintLayout backgroundLayout = (ConstraintLayout) findViewById(R.id.backgroundLayout);
        uberImageView.setOnClickListener(this);
        backgroundLayout.setOnClickListener(this);
        //redirectUser();
        ParseAnalytics.trackAppOpenedInBackground(getIntent());
    }
}