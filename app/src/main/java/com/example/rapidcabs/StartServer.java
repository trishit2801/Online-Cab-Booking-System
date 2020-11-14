package com.example.rapidcabs;

import com.parse.Parse;
import android.app.Application;

public class StartServer extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("u5Re5x8S6ZH5luRt41Y8tc6H60BOcV6CSPm5fb4V")
                .clientKey("sfayoxI7AEI4nZHuCdgupFkqMyHSagRiiBcQV1CP")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}