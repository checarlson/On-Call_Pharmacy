package com.ldd.on_callpharmacy;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();


        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("jIz5VPXkZDRhw4wVslrL7kxsAstvdPwIF0zyB2LD")
                // if defined
                .clientKey("kCQrFBQfgQ2aFRM7wH2Rz7jjmmsgfvFfqfSbdh9O")
                .server("https://parseapi.back4app.com/")
                .enableLocalDataStore()
                .build()
        );
    }
}
