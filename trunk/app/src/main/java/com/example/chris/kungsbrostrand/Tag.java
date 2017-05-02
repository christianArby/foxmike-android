package com.example.chris.kungsbrostrand;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

/**
 * Created by chris on 2017-04-19.
 */

public class Tag extends Application {
    public void onCreate() {
        super.onCreate();

        // Newer version

        if(!com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {

            FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        }

    }
}
