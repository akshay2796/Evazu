package com.evazu;

import android.app.Application;

import com.firebase.client.Firebase;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;

public class Evazu extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if(!FirebaseApp.getApps(this).isEmpty()) {

           FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

       // Firebase.setAndroidContext(this);
    }
}
